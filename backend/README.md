# VentureForge AI — Production Backend Deployment Guide

This backend service acts as a secure reverse proxy and moderation filter between the VentureForge AI Android application and external AI providers (Google Gemini).

## Key Responsibilities
1. **Credential Isolation**: Protects `GEMINI_API_KEY` on the server side so client APKs never contain secrets.
2. **Schema & Input Validation**: Validates all incoming venture payloads and rejects malicious requests before reaching the AI provider.
3. **Rate Limiting & Cost Control**: Enforces IP-based sliding window rate limits (30 req/15min) to prevent abuse and denial-of-wallet.
4. **Content Reporting Pipeline**: Receives user-reported AI outputs from the Android app via `/api/report-content` for moderation.

## Environment Variables
- `PORT`: (Default: `8080`)
- `GEMINI_API_KEY`: Google Gemini API Key (keep private on server)

## Deploying to Google Cloud Run (Recommended)
```bash
# 1. Build and push container to Google Artifact Registry
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/ventureforge-backend

# 2. Deploy to Cloud Run
gcloud run deploy ventureforge-backend \
  --image gcr.io/YOUR_PROJECT_ID/ventureforge-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars GEMINI_API_KEY=your_production_gemini_key

# 3. Enter the provided Cloud Run HTTPS URL into the VentureForge AI Android Settings screen.
```

## Running Locally for Testing
```bash
cd backend
npm install
export GEMINI_API_KEY="your_api_key"
npm run dev
# Android Emulator accesses host via http://10.0.2.2:8080
```
