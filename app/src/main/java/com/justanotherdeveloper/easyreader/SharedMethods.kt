package com.justanotherdeveloper.easyreader

import android.app.Activity
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.transition.TransitionManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Returns number of days between 2 dates
fun daysBetweenDates(startMonth: Int, startDay: Int, startYear: Int,
                     endMonth: Int, endDay: Int, endYear: Int): Int {
    return daysBetween(
        createCalendar(startYear, startMonth, startDay),
        createCalendar(endYear, endMonth, endDay)
    )
}

// Returns number of days between 2 Calendar type dates
fun daysBetween(startDate: Calendar, endDate: Calendar): Int {
    startDate.resetTimeOfDay()
    endDate.resetTimeOfDay()
    val end = endDate.timeInMillis
    val start = startDate.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(abs(end - start)).toInt()
}

// takes the day month and year and returns date as a Calendar object
fun createCalendar(year: Int, month: Int, day: Int): Calendar {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month-1)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    return calendar.resetTimeOfDay()
}

// sets calendar object to start of day (12:00:00:00 AM)
fun Calendar.resetTimeOfDay(): Calendar {
    set(Calendar.MILLISECOND, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.HOUR_OF_DAY, 0)
    return this
}

// counts words in a string
fun countWords(s: String): Int {
    var wordCount = 0

    var word = false
    val endOfLine = s.length - 1

    for (i in s.indices) {
        // if the char is a letter, word = true.
        if (Character.isLetter(s[i]) && i != endOfLine) {
            word = true
            // if char isn't a letter and there have been letters before,
            // counter goes up.
        } else if (!Character.isLetter(s[i]) && word) {
            wordCount++
            word = false
            // last word of String; if it doesn't end with a non letter, it
            // wouldn't count without this.
        } else if (Character.isLetter(s[i]) && i == endOfLine) {
            wordCount++
        }
    }
    return wordCount
}

// converts milliseconds to time string 0:00
fun getTimeString(milliseconds: Int): String {
    val totalSeconds = milliseconds.toDouble() / 1000.0
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    var secondsString = seconds.toString()
    if(secondsString.length == 1)
        secondsString = "0$secondsString"
    return "$minutes:$secondsString"
}

// returns the milliseconds per word based on the reading speed
fun Activity.getMSPW(speed: String): Long {
    return when(speed) {
        resources.getString(R.string.speedSetting1) -> mspwForPt5xSpd
        resources.getString(R.string.speedSetting2) -> mspwFor1xSpd
        resources.getString(R.string.speedSetting3) -> mspwFor1Pt5xSpd
        resources.getString(R.string.speedSetting4) -> mspwFor2xSpd
        else -> mspwFor2pt5xSpd
    }
}

// trims out new lines, spaces, and tabs at the beginning & end of string
fun trimTextSpaces(text: String): String {
    var subtitle = text
    if(subtitle.isEmpty()) return subtitle
    while(subtitle[0] == '\n' || subtitle[0] == ' ' || subtitle[0] == '\t') {
        subtitle = subtitle.substring(1, subtitle.length)
        if(subtitle.isEmpty()) return subtitle
    }
    while(subtitle[subtitle.lastIndex] == '\n'
        || subtitle[subtitle.lastIndex] == ' '
        || subtitle[subtitle.lastIndex] == '\t') {
        subtitle = subtitle.substring(0, subtitle.lastIndex)
        if(subtitle.isEmpty()) return subtitle
    }
    return subtitle
}

// sets color for drawables
fun Drawable.setColorFilter(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        this.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
    else this.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

// sets font style for text views
fun TextView.setFontStyle(activity: AppCompatActivity, fontStyle: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setTextAppearance(fontStyle)
    else setTextAppearance(activity, fontStyle)
}

// prepares for transition animation
fun beginTransition(layout: LinearLayout) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        TransitionManager.beginDelayedTransition(layout)
}