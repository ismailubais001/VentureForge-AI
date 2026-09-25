import express, { Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';

// Security headers & middleware
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

// Rate limiting: max 30 requests per 15 minutes per IP
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 30,
  message: { error: 'Too many requests from this IP, please try again later.' },
  standardHeaders: true,
  legacyHeaders: false,
});
app.use('/api/', limiter);

// In-memory reports store (in production, use Cloud SQL / PostgreSQL)
interface StoredReport {
  id: string;
  ventureId: number;
  responseSnippet: string;
  category: string;
  userComment: string;
  timestamp: number;
  receivedAt: string;
}
const contentReports: StoredReport[] = [];

// Health check endpoint
app.get('/api/health', (req: Request, res: Response) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    service: 'VentureForge AI Production Proxy',
  });
});

// Analyze venture endpoint
app.post('/api/analyze', async (req: Request, res: Response) => {
  try {
    const { venture } = req.body;
    if (!venture || !venture.name || !venture.businessIdea) {
      return res.status(400).json({ error: 'Invalid venture payload. Name and businessIdea required.' });
    }

    if (!GEMINI_API_KEY) {
      return res.status(503).json({
        error: 'Server AI provider key not configured. Set GEMINI_API_KEY in server environment.',
      });
    }

    const prompt = `
You are VentureForge AI, a top-tier venture strategist, startup evaluator, and financial analyst.
Analyze the following startup venture thoroughly, objectively, and realistically.
Do not guarantee success; evaluate real risks, market dynamics, and execution challenges.

VENTURE DETAILS:
- Name: ${venture.name}
- Industry: ${venture.industry}
- Target Market: ${venture.targetMarket}
- Target Audience: ${venture.targetAudience}
- Business Idea: ${venture.businessIdea}
- Customer Problem: ${venture.customerProblem}
- Proposed Solution: ${venture.proposedSolution}
- Business Model: ${venture.businessModel}
- Startup Budget: $${venture.estimatedStartupBudget}
- Monthly Budget: $${venture.monthlyOperatingBudget}
- Team Size: ${venture.teamSize}
- Revenue Model: ${venture.revenueModel}
- Competitors: ${venture.competitors}
- Unique Selling Proposition: ${venture.uniqueSellingProposition}

Return ONLY valid JSON matching this schema:
{
  "executiveSummary": "2-3 sentences overview.",
  "problemAnalysis": "Assessment of customer problem urgency.",
  "solutionEvaluation": "Feasibility and defensibility of solution.",
  "targetMarketValidation": "Market size and customer acquisition realities.",
  "businessModelFit": "Evaluation of margin potential and recurring revenue.",
  "valuePropositionAssessment": "Clarity of value proposition compared to substitutes.",
  "strengths": ["string", "string", "string", "string"],
  "weaknesses": ["string", "string", "string", "string"],
  "opportunities": ["string", "string", "string", "string"],
  "threats": ["string", "string", "string", "string"],
  "competitors": [
    {
      "name": "Competitor Name",
      "differentiation": "Key difference",
      "threatLevel": "High | Medium | Low",
      "marketPositioning": "Current positioning",
      "notes": "Estimated strategy (AI hypothesis)"
    }
  ],
  "marketRisks": ["Risk 1", "Risk 2", "Risk 3"],
  "recommendations": ["Recommendation 1", "Recommendation 2", "Recommendation 3"],
  "actionPlan": [
    {
      "phase": "Phase 1: Validation",
      "taskName": "Specific task",
      "description": "How to execute",
      "priority": "High | Medium | Low",
      "estimatedDays": 14
    }
  ],
  "feasibilityScore": {
    "overallScore": 72,
    "problemClarityScore": 80,
    "marketDemandScore": 75,
    "competitionDifficultyScore": 65,
    "unitEconomicsScore": 70,
    "executionFeasibilityScore": 68,
    "riskLevel": "Moderate",
    "keyAssumptions": ["Assumption 1", "Assumption 2"],
    "disclaimerNote": "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success."
  },
  "confidenceNotes": ["AI evaluation based on user inputs. Market validation required."]
}
    `.trim();

    // Call Gemini REST API securely on server-side
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`;
    const response = await fetch(geminiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.3,
          responseMimeType: 'application/json',
        },
      }),
    });

    if (!response.ok) {
      const errText = await response.text();
      console.error('Gemini Provider Error:', errText);
      return res.status(502).json({ error: 'AI provider service temporarily unavailable.' });
    }

    const data = await response.json();
    const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!rawText) {
      return res.status(502).json({ error: 'AI provider returned empty response.' });
    }

    const parsedJson = JSON.parse(rawText);
    res.json(parsedJson);
  } catch (error: any) {
    console.error('Analyze Error:', error);
    res.status(500).json({ error: 'Internal server error while processing analysis.' });
  }
});

// Report content endpoint
app.post('/api/report-content', (req: Request, res: Response) => {
  try {
    const { ventureId, responseSnippet, category, userComment, timestamp } = req.body;
    const report: StoredReport = {
      id: `rep_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      ventureId: Number(ventureId) || 0,
      responseSnippet: String(responseSnippet || '').slice(0, 500),
      category: String(category || 'Other'),
      userComment: String(userComment || '').slice(0, 1000),
      timestamp: Number(timestamp) || Date.now(),
      receivedAt: new Date().toISOString(),
    };
    contentReports.push(report);
    console.log(`[REPORT RECEIVED] Category: ${report.category}, VentureId: ${report.ventureId}`);
    res.status(201).json({ success: true, reportId: report.id });
  } catch (error) {
    console.error('Report Error:', error);
    res.status(500).json({ error: 'Failed to record report.' });
  }
});

app.listen(PORT, () => {
  console.log(`VentureForge AI Backend listening on port ${PORT}`);
});
