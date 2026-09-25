package com.example.data.database.converters

import androidx.room.TypeConverter
import com.example.domain.model.ActionMilestone
import com.example.domain.model.CompetitorInsight
import com.example.domain.model.FeasibilityScore
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val list = ArrayList<String>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.optString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        fun serializeStringList(list: List<String>): String {
            val array = JSONArray()
            for (item in list) array.put(item)
            return array.toString()
        }

        fun deserializeStringList(json: String): List<String> {
            if (json.isBlank()) return emptyList()
            val list = ArrayList<String>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    list.add(array.optString(i))
                }
            } catch (_: Exception) {}
            return list
        }

        fun serializeCompetitors(list: List<CompetitorInsight>): String {
            val array = JSONArray()
            for (c in list) {
                val obj = JSONObject()
                obj.put("name", c.name)
                obj.put("differentiation", c.differentiation)
                obj.put("threatLevel", c.threatLevel)
                obj.put("marketPositioning", c.marketPositioning)
                obj.put("notes", c.notes)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeCompetitors(json: String): List<CompetitorInsight> {
            if (json.isBlank()) return emptyList()
            val list = ArrayList<CompetitorInsight>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    list.add(
                        CompetitorInsight(
                            name = obj.optString("name", "Competitor"),
                            differentiation = obj.optString("differentiation", ""),
                            threatLevel = obj.optString("threatLevel", "Moderate"),
                            marketPositioning = obj.optString("marketPositioning", ""),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        fun serializeActionPlan(list: List<ActionMilestone>): String {
            val array = JSONArray()
            for (m in list) {
                val obj = JSONObject()
                obj.put("phase", m.phase)
                obj.put("taskName", m.taskName)
                obj.put("description", m.description)
                obj.put("priority", m.priority)
                obj.put("estimatedDays", m.estimatedDays)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeActionPlan(json: String): List<ActionMilestone> {
            if (json.isBlank()) return emptyList()
            val list = ArrayList<ActionMilestone>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    list.add(
                        ActionMilestone(
                            phase = obj.optString("phase", "Phase"),
                            taskName = obj.optString("taskName", "Task"),
                            description = obj.optString("description", ""),
                            priority = obj.optString("priority", "Medium"),
                            estimatedDays = obj.optInt("estimatedDays", 7)
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        fun serializeFeasibilityScore(score: FeasibilityScore): String {
            val obj = JSONObject()
            obj.put("overallScore", score.overallScore)
            obj.put("problemClarityScore", score.problemClarityScore)
            obj.put("marketDemandScore", score.marketDemandScore)
            obj.put("competitionDifficultyScore", score.competitionDifficultyScore)
            obj.put("unitEconomicsScore", score.unitEconomicsScore)
            obj.put("executionFeasibilityScore", score.executionFeasibilityScore)
            obj.put("riskLevel", score.riskLevel)
            obj.put("keyAssumptions", JSONArray(score.keyAssumptions))
            obj.put("disclaimerNote", score.disclaimerNote)
            return obj.toString()
        }

        fun deserializeFeasibilityScore(json: String): FeasibilityScore {
            if (json.isBlank()) {
                return FeasibilityScore(
                    overallScore = 65,
                    problemClarityScore = 70,
                    marketDemandScore = 65,
                    competitionDifficultyScore = 60,
                    unitEconomicsScore = 70,
                    executionFeasibilityScore = 60,
                    riskLevel = "Moderate",
                    keyAssumptions = listOf("Assumptions based on initial user inputs")
                )
            }
            return try {
                val obj = JSONObject(json)
                val assumptionsList = ArrayList<String>()
                val arr = obj.optJSONArray("keyAssumptions")
                if (arr != null) {
                    for (i in 0 until arr.length()) assumptionsList.add(arr.optString(i))
                }
                FeasibilityScore(
                    overallScore = obj.optInt("overallScore", 65),
                    problemClarityScore = obj.optInt("problemClarityScore", 70),
                    marketDemandScore = obj.optInt("marketDemandScore", 65),
                    competitionDifficultyScore = obj.optInt("competitionDifficultyScore", 60),
                    unitEconomicsScore = obj.optInt("unitEconomicsScore", 70),
                    executionFeasibilityScore = obj.optInt("executionFeasibilityScore", 60),
                    riskLevel = obj.optString("riskLevel", "Moderate"),
                    keyAssumptions = if (assumptionsList.isEmpty()) listOf("Assumptions based on initial venture data") else assumptionsList,
                    disclaimerNote = obj.optString("disclaimerNote", "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success.")
                )
            } catch (_: Exception) {
                FeasibilityScore(
                    overallScore = 65,
                    problemClarityScore = 70,
                    marketDemandScore = 65,
                    competitionDifficultyScore = 60,
                    unitEconomicsScore = 70,
                    executionFeasibilityScore = 60,
                    riskLevel = "Moderate",
                    keyAssumptions = listOf("Assumptions based on initial venture data")
                )
            }
        }
    }
}
