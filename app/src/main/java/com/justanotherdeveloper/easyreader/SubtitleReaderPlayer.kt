package com.justanotherdeveloper.easyreader

import android.os.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import java.util.*
import kotlin.collections.HashMap

// Back end functionality of the Subtitle Set player from Subtitle Reader Page
@Suppress("DEPRECATION")
class SubtitleReaderPlayer (private val activity:SubtitleReaderActivity) {

    private lateinit var textToSpeech: TextToSpeech

    private var readWords = 0.0
    private var totalWords = 0
    private var totalMS = 0
    private var mspw: Long = 0
    private var textToSpeechIsFunctional = false
    private var readingOnlyTimer = Timer()
    private var textToSpeechTimer = Timer()
    private val textToSpeechMap = HashMap<String, String>()
    private val handler = Handler()

    var isPlaying = false

    init {
        initTextToSpeech()
        updateTotalWords()
        updateMSPW()
        updateTotalTime()
    }

    private fun initTextToSpeech() {
        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if(!isPlaying) handler.post { pause() }
            }
            override fun onDone(utteranceId: String?) {
                handler.post { activity.moveToNextSubtitle() }
            }
            override fun onError(utteranceId: String?) {
                handler.postDelayed({ if(isPlaying && !textToSpeech.isSpeaking)
                    playTextToSpeech() }, subtitleStartDelay)
            }
        }

        textToSpeech = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
            if(status != TextToSpeech.ERROR) {
                textToSpeechIsFunctional = true
                textToSpeech.language = Locale.US
                textToSpeechMap[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceID
                textToSpeech.setOnUtteranceProgressListener(speechListener)
                updateSpeechRate()
            }
            handler.post { activity.playerIsReady() }
        })
    }

    fun updateTotalWords() {
        totalWords = countWords(activity.subtitleSet.enteredText)
    }

    private fun updateMSPW() {
        mspw = activity.getMSPW(activity.subtitleSet.speed)
    }

    fun updateTotalTime() {
        totalMS = (mspw * totalWords + activity.subtitles.size * subtitleStartDelay).toInt()
    }

    fun getReadWords(): Int {
        return readWords.toInt()
    }

    fun getTotalWords(): Int {
        return totalWords
    }

    fun getNWordsInSubtitle(): Int {
        return countWords(activity.currentSubtitle)
    }

    fun getProgress(nWordsRead: Double): Int {
        readWords += nWordsRead
        return ((readWords / totalWords.toDouble()) * 1000).toInt()
    }

    fun setProgress(readWords: Double): Int {
        this.readWords = readWords
        return ((readWords / totalWords.toDouble()) * 1000).toInt()
    }

    fun calculateProgress(startIndex: Int): Int {
        var nWords = 0
        for(i in 0 until startIndex)
            nWords += countWords(modifySubtitle(i))
        return setProgress(nWords.toDouble())
    }

    fun getTotalMilliseconds(): Int {
        return totalMS
    }

    fun updateSpeed() {
        updateSpeechRate()
        updateMSPW()
        updateTotalTime()
    }

    private fun updateSpeechRate() {
        textToSpeech.setSpeechRate(getSpeechRate(activity.subtitleSet.speed))
    }

    private fun getSpeechRate(speed: String): Float {
        return when(speed) {
            activity.resources.getString(R.string.speedSetting1) -> .75f
            activity.resources.getString(R.string.speedSetting2) -> 1f
            activity.resources.getString(R.string.speedSetting3) -> 1.5f
            activity.resources.getString(R.string.speedSetting4) -> 2f
            else -> 2.5f
        }
    }

    fun start() {
        isPlaying = true
        if(activity.subtitleSet.textToSpeechEnabled)
            playTextToSpeech()
        else playForReadingOnly()
    }

    private fun playTextToSpeech() {
        if(textToSpeechIsFunctional) {
            textToSpeechTimer.cancel()
            textToSpeechTimer = Timer()
            textToSpeechTimer.schedule(object: TimerTask() {
                override fun run() { handler.post { if(isPlaying) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        textToSpeech.speak(activity.currentSubtitle,
                            TextToSpeech.QUEUE_FLUSH, null, utteranceID)
                    else textToSpeech.speak(activity.currentSubtitle,
                        TextToSpeech.QUEUE_FLUSH, textToSpeechMap)
                } }
                }
            }, subtitleStartDelay)
        } else activity.disableTextToSpeech()
    }

    private fun playForReadingOnly() {
        readingOnlyTimer.schedule(object: TimerTask() {
            override fun run() { handler.post { activity.moveToNextSubtitle() } }
        }, getSubtitleDuration())
    }

    // returns duration of current subtitle
    fun getSubtitleDuration(): Long {
        // milliseconds per word
        val nWordsInSubtitle = countWords(activity.currentSubtitle)
        return mspw * nWordsInSubtitle + subtitleStartDelay
    }

    fun pause() {
        textToSpeechTimer.cancel()
        if(isPlaying) {
            stopTextToSpeech()
            stopReadingOnlyTimer()
        }
        isPlaying = false
    }

    // stops text to speech
    private fun stopTextToSpeech() {
        if(textToSpeechIsFunctional
            && textToSpeech.isSpeaking)
            textToSpeech.stop()
    }

    // pauses player when text to speech is off
    private fun stopReadingOnlyTimer() {
        readingOnlyTimer.cancel()
        readingOnlyTimer = Timer()
    }

    // checks if there is a next subtitle
    // and then makes it the current subtitle
    fun hasNextSubtitle(): Boolean {
        if(++activity.currentSubtitleIndex >= activity.subtitles.size)
            return finishPlayer()
        val nextSubtitle = getNextSubtitle()
        if(nextSubtitle.isNotEmpty())
            activity.currentSubtitle = nextSubtitle
        else return finishPlayer()
        return true
    }

    // stops the subtitle reader when all subtitles are finished
    private fun finishPlayer(): Boolean {
        activity.finishSubtitleReader()
        isPlaying = false
        return false
    }

    // gets the next subtitle that is not empty
    fun getNextSubtitle(): String {
        var subtitle = ""
        while(subtitle.isEmpty()) {
            subtitle = modifySubtitle()
            if(subtitle.isEmpty())
                activity.currentSubtitleIndex++
            if(activity.currentSubtitleIndex == activity.subtitles.size) {
                activity.currentSubtitleIndex = activity.subtitles.size
                break
            }
        }
        return subtitle
    }

    // checks if there is a previous subtitle
    // and then makes it the current subtitle
    fun hasPreviousSubtitle(): Boolean {
        if(activity.currentSubtitleIndex == activity.subtitles.size)
            getPreviousSubtitle()
        if(activity.currentSubtitleIndex - 1 < 0) return false
        val previousSubtitle = getPreviousSubtitle()
        if(previousSubtitle.isNotEmpty())
            activity.currentSubtitle = previousSubtitle
        else return false
        return true
    }

    // gets the previous subtitle that is not empty
    private fun getPreviousSubtitle(): String {
        var subtitle = ""
        var index = activity.currentSubtitleIndex - 1
        while(subtitle.isEmpty()) {
            subtitle = modifySubtitle(index)
            if(subtitle.isEmpty())
                index--
            else activity.currentSubtitleIndex = index
            if(index < 0) break
        }
        return subtitle
    }
    // trims out new lines, spaces, and tabs at the beginning & end of string
    fun modifySubtitle(index: Int = activity.currentSubtitleIndex): String {
        return trimTextSpaces(activity.subtitles[index])
    }

    fun shutdownTextToSpeech() {
        stopTextToSpeech()
        textToSpeech.shutdown()
    }
}