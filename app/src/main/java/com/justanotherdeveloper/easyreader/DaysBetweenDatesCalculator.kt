package com.justanotherdeveloper.easyreader

// outdated code

/*
fun daysBetweenDates(startMonth: Int, startDay: Int, startYear: Int,
                     endMonth: Int, endDay: Int, endYear: Int): Int {
    return if (startYear == endYear)
        getNDaysBetweenDatesWithinYear(startMonth, startDay, endMonth, endDay, startYear)
    else {
        val nDaysBetweenYears =
            (endYear - 1 - startYear) * 365 + getNLeapYears(startYear, endYear)
        getNDaysUntilEndOfYear(startMonth, startDay, startYear) +
                nDaysBetweenYears + getNDaysSinceStartOfYear(endMonth, endDay, endYear)
    }
}

private fun getNLeapYears(startYear: Int, endYear: Int): Int {
    var nLeapYears = 0
    var startCount = endYear - 1
    while (!isLeapYear(startCount)) startCount--
    var endCount = startYear + 1
    while (!isLeapYear(endCount)) endCount++
    for (year in startCount downTo endCount step 4)
        if (isLeapYear(year)) nLeapYears++
    return nLeapYears
}

private fun getNDaysBetweenDatesWithinYear(startMonth: Int, startDay: Int,
                                           endMonth: Int, endDay: Int, year: Int): Int {
    if (startMonth == endMonth) return endDay - startDay
    return getNDaysUntilEndOfMonth(startMonth, startDay, year) +
            getNDaysBetweenMonths(startMonth, endMonth, year) + endDay
}

private fun getNDaysBetweenMonths(startMonth: Int, endMonth: Int, year: Int): Int {
    var nDaysBetweenMonths = 0
    if (startMonth < 2 && 2 < endMonth) nDaysBetweenMonths += if (isLeapYear(year)) 29 else 28
    if (startMonth < 3 && 3 < endMonth) nDaysBetweenMonths += 31
    if (startMonth < 4 && 4 < endMonth) nDaysBetweenMonths += 30
    if (startMonth < 5 && 5 < endMonth) nDaysBetweenMonths += 31
    if (startMonth < 6 && 6 < endMonth) nDaysBetweenMonths += 30
    if (startMonth < 7 && 7 < endMonth) nDaysBetweenMonths += 31
    if (startMonth < 8 && 8 < endMonth) nDaysBetweenMonths += 31
    if (startMonth < 9 && 9 < endMonth) nDaysBetweenMonths += 30
    if (startMonth < 10 && 10 < endMonth) nDaysBetweenMonths += 31
    if (startMonth < 11 && 11 < endMonth) nDaysBetweenMonths += 30
    return nDaysBetweenMonths
}

private fun getNDaysUntilEndOfMonth(month: Int, day: Int, year: Int): Int {
    return when (month) {
        1 -> 31 - day
        2 -> if (isLeapYear(year)) 29 - day else 28 - day
        3 -> 31 - day
        4 -> 30 - day
        5 -> 31 - day
        6 -> 30 - day
        7 -> 31 - day
        8 -> 31 - day
        9 -> 30 - day
        10 -> 31 - day
        11 -> 30 - day
        else -> 31 - day
    }
}

private fun getNDaysUntilEndOfYear(month: Int, day: Int, year: Int): Int {
    val nDaysUntilEndOfMonth = getNDaysUntilEndOfMonth(month, day, year)
    var nDaysUntilEndOfYear = 0
    if (2 > month) nDaysUntilEndOfYear += if (isLeapYear(year)) 29 else 28
    if (3 > month) nDaysUntilEndOfYear += 31
    if (4 > month) nDaysUntilEndOfYear += 30
    if (5 > month) nDaysUntilEndOfYear += 31
    if (6 > month) nDaysUntilEndOfYear += 30
    if (7 > month) nDaysUntilEndOfYear += 31
    if (8 > month) nDaysUntilEndOfYear += 31
    if (9 > month) nDaysUntilEndOfYear += 30
    if (10 > month) nDaysUntilEndOfYear += 31
    if (11 > month) nDaysUntilEndOfYear += 30
    if (12 > month) nDaysUntilEndOfYear += 31
    return nDaysUntilEndOfMonth + nDaysUntilEndOfYear
}

private fun getNDaysSinceStartOfYear(month: Int, day: Int, year: Int): Int {
    var nDaysSinceStartOfYear = 0
    if (month > 11) nDaysSinceStartOfYear += 30
    if (month > 10) nDaysSinceStartOfYear += 31
    if (month > 9) nDaysSinceStartOfYear += 30
    if (month > 8) nDaysSinceStartOfYear += 31
    if (month > 7) nDaysSinceStartOfYear += 31
    if (month > 6) nDaysSinceStartOfYear += 30
    if (month > 5) nDaysSinceStartOfYear += 31
    if (month > 4) nDaysSinceStartOfYear += 30
    if (month > 3) nDaysSinceStartOfYear += 31
    if (month > 2) nDaysSinceStartOfYear += if (isLeapYear(year)) 29 else 28
    if (month > 1) nDaysSinceStartOfYear += 31
    return day + nDaysSinceStartOfYear
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}
*/