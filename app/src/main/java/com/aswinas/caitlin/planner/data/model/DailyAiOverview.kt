package com.aswinas.caitlin.planner.data.model

/**
 * AI-generated daily overview. Empty values represent the real empty state before
 * the user has generated an overview; no synthetic/sample metrics are used.
 */
data class DailyAiOverview(
    val headline: String = "",
    val focusScore: Int = 0,
    val velocityGrade: String = "",
    val progressAnalysis: String = "",
    val actionableTips: List<String> = emptyList(),
    val energyScheduleAdvice: String = "",
    val recommendedSlots: List<String> = emptyList(),
    val lastUpdated: String = "",
    val preferenceFocus: String = ""
) {
    val velocityPace: String get() = velocityGrade
    val progressSummary: String get() = progressAnalysis
    val keyTips: List<String> get() = actionableTips
    val lastUpdatedFormatted: String get() = lastUpdated
}
