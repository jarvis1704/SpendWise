@file:Suppress("unused")

package com.biprangshu.spendwise.ui.components.datepicker

import android.content.Context
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

/** Returns the days of the week ordered from the locale's first day. */
fun getWeek(): List<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val days = DayOfWeek.values()
    val startIndex = days.indexOf(firstDayOfWeek)
    return days.drop(startIndex) + days.take(startIndex)
}

/** Returns the narrow display name for a day of week (e.g. "M", "T"). */
fun prettyWeekDay(day: DayOfWeek): String =
    day.getDisplayName(TextStyle.NARROW, Locale.getDefault())

/** Returns a formatted "Month Year" string (e.g. "January 2025"). */
fun prettyYearMonth(yearMonth: YearMonth): String =
    yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))

/**
 * Returns the number of week-rows needed to display this month in a calendar grid,
 * respecting the locale's first day of week.
 */
fun YearMonth.getNumberWeeks(@Suppress("UNUSED_PARAMETER") context: Context): Int {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val firstOfMonth = this.atDay(1)
    val offset = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    return ceil((offset + this.lengthOfMonth()).toDouble() / 7).toInt()
}

/** Converts a [Date] to a [LocalDate] in the system default time zone. */
fun Date.toLocalDate(): LocalDate =
    this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

/** Converts a [LocalDate] to a [Date] at midnight in the system default time zone. */
fun LocalDate.toDate(): Date =
    Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

/** Returns true if both epoch-millisecond timestamps fall on the same calendar day. */
fun isSameDay(ms1: Long, ms2: Long): Boolean {
    val z = ZoneId.systemDefault()
    return Instant.ofEpochMilli(ms1).atZone(z).toLocalDate() ==
            Instant.ofEpochMilli(ms2).atZone(z).toLocalDate()
}

/**
 * Formats a date for display.
 * When [forceShowDate] is true (or the date is not today/yesterday) it returns "d MMM".
 * When [showTime] is false, time is omitted regardless.
 */
fun prettyDate(date: Date, showTime: Boolean = true, forceShowDate: Boolean = false): String {
    val localDate = date.toLocalDate()
    val today = LocalDate.now()
    return when {
        !forceShowDate && localDate.isEqual(today) -> "Today"
        !forceShowDate && localDate.isEqual(today.minusDays(1)) -> "Yesterday"
        else -> localDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}
