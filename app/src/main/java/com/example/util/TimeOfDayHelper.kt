package com.example.util

import java.util.Calendar

enum class TimeOfDayPeriod {
    MORNING,    // 05:00 - 11:59
    AFTERNOON,  // 12:00 - 16:59
    EVENING,    // 17:00 - 20:59
    NIGHT       // 21:00 - 04:59
}

object TimeOfDayHelper {
    fun getCurrentPeriod(): TimeOfDayPeriod {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> TimeOfDayPeriod.MORNING
            in 12..16 -> TimeOfDayPeriod.AFTERNOON
            in 17..20 -> TimeOfDayPeriod.EVENING
            else -> TimeOfDayPeriod.NIGHT
        }
    }

    /**
     * Short greeting subtext based on the time of the day to keep it concise and punchy.
     */
    fun getGreetingSubtext(period: TimeOfDayPeriod): String {
        return when (period) {
            TimeOfDayPeriod.MORNING -> "Fresh start awaits"
            TimeOfDayPeriod.AFTERNOON -> "Keep up the momentum"
            TimeOfDayPeriod.EVENING -> "Golden hour focus"
            TimeOfDayPeriod.NIGHT -> "Quiet focus & rest"
        }
    }

    fun getPeriodLabel(period: TimeOfDayPeriod): String {
        return when (period) {
            TimeOfDayPeriod.MORNING -> "Morning Focus"
            TimeOfDayPeriod.AFTERNOON -> "Afternoon Momentum"
            TimeOfDayPeriod.EVENING -> "Evening Flow"
            TimeOfDayPeriod.NIGHT -> "Night Reset"
        }
    }
}
