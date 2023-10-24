package com.justanotherdeveloper.easyreader

const val minEnteredWordsPerSubtitle = 1
const val maxEnteredWordsPerSubtitle = 50

const val maxCharSubtitleToShow = 35
const val maxCharSubtitleToRemove = 50
const val maxCharPerSubtitle = 250
const val maxNewLinesPerSubtitle = 5

const val idsFilename = "subtitle set ids"

const val autoplayRef = "autoplay"
const val subtitlesStartRef = "subtitles start"
const val subtitlesUpdatedRef = "subtitles updated"
const val dbUpdatedRef = "db updated"
const val idRef = "set id"

const val doubleTapDelay: Long = 250
const val subtitleStartDelay: Long = 100
const val longPressRepeatDelay: Long = 100
const val hideUITimerDuration: Long = 3000

const val utteranceID = "UniqueID"

const val webText = "http://"
const val webTextSecure = "https://"

// mspw = milliseconds per word for various speeds:
// .5x (.75), 1x, 1.5x, 2x, 2.5x
const val mspwForPt5xSpd: Long = 470
const val mspwFor1xSpd: Long = 310
const val mspwFor1Pt5xSpd: Long = 295
const val mspwFor2xSpd: Long = 270
const val mspwFor2pt5xSpd: Long = 220