package com.justanotherdeveloper.easyreader

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_subtitle_reader.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.SeekBar
import android.view.MotionEvent

class SubtitleReaderActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener  {

    private lateinit var view: SubtitleReaderViewMethods
    private lateinit var manager: SubtitleSetManager
    private lateinit var player: SubtitleReaderPlayer

    lateinit var subtitleSet: SubtitleSetManager.SubtitleSet
    lateinit var subtitles: ArrayList<String>

    private val handler = Handler()
    private var hideUITimer = Timer()
    private var autoplayTimer = Timer()

    private var continuousPreviousButtonPressTimer = Timer()
    private var continuousNextButtonPressTimer = Timer()

    private val requestCode = 1
    private var startNWords = 0
    private var endNWords = 0
    private var autoplayIsActive = false
    private var playerWasPlaying = false

    var currentSubtitle = ""
    var currentSubtitleIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtitle_reader)
        val id = intent.getIntExtra(idRef, 0)
        manager = SubtitleSetManager(this)
        subtitleSet = manager.getSubtitleSet(id.toString())
        subtitles = subtitleSet.subtitles
        player = SubtitleReaderPlayer(this)
        currentSubtitle = player.getNextSubtitle()
        view = SubtitleReaderViewMethods(this)
        updateUITotalTime()
        disableButtons()
        initListeners()
    }

    // displays total time of saved reading
    private fun updateUITotalTime() {
        view.setTotalTime(getTimeString(
            player.getTotalMilliseconds()))
    }

    // called by player when fully initialized
    fun playerIsReady() {
        enableButtons()
        if(currentSubtitle.isNotEmpty()
            && subtitleSet.savedSubtitleIndex != -1
            && subtitleSet.savedSubtitleIndex != subtitles.size)
            skipToSubtitle(subtitleSet.savedSubtitleIndex)
        else view.showCurrentSubtitle()
    }

    // when player is ready, buttons are enabled
    private fun enableButtons() {
        backArrow.isEnabled = true
        pausePlayButton.isEnabled = true
        nextButton.isEnabled = true
        previousButton.isEnabled = true
        leftScreen.isEnabled = true
        rightScreen.isEnabled = true
        settingsButton.isEnabled = true
        subtitleListButton.isEnabled = true
        moreButton.isEnabled = true
    }

    // buttons are disabled until player is ready
    private fun disableButtons() {
        backArrow.isEnabled = false
        pausePlayButton.isEnabled = false
        nextButton.isEnabled = false
        previousButton.isEnabled = false
        leftScreen.isEnabled = false
        rightScreen.isEnabled = false
        settingsButton.isEnabled = false
        subtitleListButton.isEnabled = false
        moreButton.isEnabled = false
    }

    private fun initListeners() {
        initNextButtonListeners()
        initPreviousButtonListeners()
        initLeftScreenListener()
        initRightScreenListener()
        initSeekbarListener()

        backArrow.setOnClickListener { initFinishProcess() }

        pausePlayButton.setOnTouchListener { _, _ ->
            cancelContinuousTimers()
            pausePlayButtonPressed()
            false
        }

        settingsButton.setOnClickListener {
            if(player.isPlaying) pauseSubtitleReader()
            view.showSettings()
        }

        subtitleListButton.setOnClickListener {
            if(player.isPlaying) pauseSubtitleReader()
            val subtitleListPage = Intent(this, SubtitleListActivity::class.java)
            subtitleListPage.putExtra(idRef, subtitleSet.id)
            startActivityForResult(subtitleListPage, requestCode)
        }

        moreButton.setOnClickListener {
            if(player.isPlaying) pauseSubtitleReader()
            val popup = PopupMenu(this, moreButton)
            popup.setOnMenuItemClickListener(this)
            popup.inflate(R.menu.menu_subtitle_reader)
            if(subtitleSet.sourceLink == "")
                popup.menu.removeItem(R.id.openSourceLink)
            if(currentSubtitle.isEmpty()) {
                popup.menu.removeItem(R.id.removeThisSubtitle)
                popup.menu.removeItem(R.id.editThisSubtitle) }
            popup.show()
        }
    }

    // on touch, goes to next subtitle
    // on long press, fast forwards subtitles
    private fun initNextButtonListeners() {
        var nextButtonLongPressed = false
        fun startContinuousNextButtonPress() {
            continuousNextButtonPressTimer.cancel()
            continuousNextButtonPressTimer = Timer()
            continuousNextButtonPressTimer.schedule(object: TimerTask() {
                override fun run() { handler.post {
                    nextButtonPressed()
                    if(nextButtonLongPressed)
                        startContinuousNextButtonPress()
                } } }, longPressRepeatDelay)
        }

        nextButton.setOnLongClickListener {
            nextButtonLongPressed = true
            startContinuousNextButtonPress()
            true
        }

        nextButton.setOnTouchListener { pView, pEvent ->
            pView.onTouchEvent(pEvent)
            // on touch
            if(pEvent.action == MotionEvent.ACTION_DOWN)
                nextButtonPressed()

            // on release
            if(pEvent.action == MotionEvent.ACTION_UP) {
                if (nextButtonLongPressed) {
                    nextButtonLongPressed = false
                    continuousNextButtonPressTimer.cancel()
                }
            }
            false
        }
    }

    // presses the next button
    private fun nextButtonPressed() {
        playerWasPlaying = player.isPlaying
        pauseSubtitleReader(false)
        val nWordsInSubtitle = player.getNWordsInSubtitle().toDouble()
        if(player.hasNextSubtitle()) forwardProgress(nWordsInSubtitle)
        else {
            view.showPlayButton()
            player.setProgress(player.getTotalWords().toDouble())
            updateViewProgress(1000)
            updateSavedSubtitleIndex()
            cancelContinuousTimers()
        }
    }

    // moves progress of player and view to the next subtitle
    private fun forwardProgress(nWordsInSubtitle: Double) {
        view.showCurrentSubtitle(false)
        updateViewProgress(player.getProgress(nWordsInSubtitle))
        startNWords = player.getReadWords()
        updateSavedSubtitleIndex()
        if(playerWasPlaying) startAutoplayTimer()
    }

    // on touch, goes to previous subtitle
    // on long press, rewinds subtitles
    private fun initPreviousButtonListeners() {
        var previousButtonLongPressed = false
        fun startContinuousPreviousButtonPress() {
            continuousPreviousButtonPressTimer.cancel()
            continuousPreviousButtonPressTimer = Timer()
            continuousPreviousButtonPressTimer.schedule(object: TimerTask() {
                override fun run() { handler.post {
                    previousButtonPressed()
                    if(previousButtonLongPressed)
                        startContinuousPreviousButtonPress()
                } } }, longPressRepeatDelay)
        }

        previousButton.setOnLongClickListener {
            previousButtonLongPressed = true
            startContinuousPreviousButtonPress()
            true
        }

        previousButton.setOnTouchListener { pView, pEvent ->
            pView.onTouchEvent(pEvent)
            // on touch
            if(pEvent.action == MotionEvent.ACTION_DOWN)
                previousButtonPressed()

            // on release
            if (pEvent.action == MotionEvent.ACTION_UP) {
                if (previousButtonLongPressed) {
                    previousButtonLongPressed = false
                    continuousPreviousButtonPressTimer.cancel()
                }
            }
            false
        }
    }

    // presses the previous button
    private fun previousButtonPressed() {
        playerWasPlaying = player.isPlaying
        pauseSubtitleReader(false)
        if(player.hasPreviousSubtitle()) rewindProgress()
        else {
            view.showUI()
            view.showPlayButton()
            cancelContinuousTimers()
        }
    }

    // moves progress of player and view to the previous subtitle
    private fun rewindProgress() {
        view.showCurrentSubtitle(false)
        val nWordsInSubtitle = player.getNWordsInSubtitle().toDouble()
        updateViewProgress(player.getProgress(-nWordsInSubtitle))
        startNWords = player.getReadWords()
        updateSavedSubtitleIndex()
        if(playerWasPlaying) startAutoplayTimer()
    }

    // autoplay if next/previous button pressed while already playing
    private fun startAutoplayTimer() {
        autoplayIsActive = true
        playerWasPlaying = false
        view.showPauseButton()
        autoplayTimer.cancel()
        autoplayTimer = Timer()
        autoplayTimer.schedule(object: TimerTask() {
            override fun run() {
                handler.post {
                    if(!player.isPlaying) {
                        autoplayIsActive = false
                        pausePlayButtonPressed()
                    }
                }
            }
        }, subtitleStartDelay)
    }

    // double tap listener class
    abstract inner class DoubleClickListener : View.OnClickListener {

        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < doubleTapDelay) {
                onDoubleClick(v)
            } else {
                onSingleClick(v)
            }
            lastClickTime = clickTime
        }

        abstract fun onSingleClick(v: View)
        abstract fun onDoubleClick(v: View)
    }

    // when screen is tapped, shows or hides UI
    private fun screenTapped() {
        handler.post {
            when {
                UIArea.visibility == View.VISIBLE -> view.hideUI()
                player.isPlaying -> startHideUITimer(true)
                else -> view.showUI()
            }
        }
    }

    // on touch: hides or shows UI (play, next/previous, settings, etc.)
    // on double tap: goes to previous subtitle
    private fun initLeftScreenListener() {
        leftScreen.setOnClickListener(object : DoubleClickListener() {

            private var doubleClickTimer = Timer()

            override fun onSingleClick(v: View) {
                cancelContinuousTimers()
                doubleClickTimer.cancel()
                doubleClickTimer = Timer()
                doubleClickTimer.schedule(object: TimerTask() {
                    override fun run() { screenTapped() } }, doubleTapDelay)
            }

            override fun onDoubleClick(v: View) {
                doubleClickTimer.cancel()
                previousButtonPressed()
            }
        })
    }

    // on touch: hides or shows UI (play, next/previous, settings, etc.)
    // on double tap: goes to next subtitle
    private fun initRightScreenListener() {
        rightScreen.setOnClickListener(object : DoubleClickListener() {

            private var doubleClickTimer = Timer()

            override fun onSingleClick(v: View) {
                cancelContinuousTimers()
                doubleClickTimer.cancel()
                doubleClickTimer = Timer()
                doubleClickTimer.schedule(object: TimerTask() {
                    override fun run() { screenTapped() } }, doubleTapDelay)
            }

            override fun onDoubleClick(v: View) {
                doubleClickTimer.cancel()
                nextButtonPressed()
            }
        })
    }

    // when seekbar is being dragged, the screen will display the appropriate text
    private fun initSeekbarListener() {
        var seekbarInUse = false
        var indexToJumpTo = -1
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(!seekbarInUse) return
                val readWordsTarget = progress.toDouble() / 1000.0 * player.getTotalWords()
                var wordCount = 0
                indexToJumpTo = -1
                for((index, subtitle) in subtitles.withIndex()) {
                    currentSubtitle = player.modifySubtitle(index)
                    if(player.modifySubtitle(index).isNotEmpty()) {
                        val subtitleWordCount = countWords(subtitle)
                        if (wordCount + subtitleWordCount >= readWordsTarget) {
                            indexToJumpTo = index
                            break
                        } else wordCount += subtitleWordCount
                    }
                }
                view.showCurrentSubtitle(false)
                view.updateCurrentTime(getCurrentTime(progress))
                if(progress == 1000) indexToJumpTo = subtitles.size
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekbarInUse = true
                playerWasPlaying = player.isPlaying
                pauseSubtitleReader()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekbarInUse = false
                if(indexToJumpTo == subtitles.size) {
                    startNWords = player.getTotalWords()
                    player.setProgress(player.getTotalWords().toDouble())
                    currentSubtitleIndex = subtitles.size
                    updateSavedSubtitleIndex()
                } else if(indexToJumpTo == -1) {
                    restartSubtitleReader()
                    pauseSubtitleReader()
                    updateSavedSubtitleIndex()
                    if(playerWasPlaying) startAutoplayTimer()
                } else skipToSubtitle(indexToJumpTo, playerWasPlaying)
                playerWasPlaying = false
            }
        })
    }

    // saves current subtitle when changed
    private fun updateSavedSubtitleIndex() {
        subtitleSet.savedSubtitleIndex = currentSubtitleIndex
        manager.updateSubtitleSet(subtitleSet)
    }

    // resets the subtitle reader to the first subtitle
    // when play button is clicked after the reader is finished
    private fun restartSubtitleReader() {
        updateViewProgress(0)
        currentSubtitleIndex = 0
        currentSubtitle = player.getNextSubtitle()
        view.showCurrentSubtitle()
        view.showPauseButton()
        player.setProgress(0.0)
        player.start()
        resumeProgress()
        startHideUITimer()
    }

    // jumps to the subtitle selected from the subtitle playlist page
    private fun skipToSubtitle(startIndex: Int, autoplay: Boolean = false) {
        restartSubtitleReader()
        pauseSubtitleReader()
        currentSubtitleIndex = startIndex
        currentSubtitle = player.getNextSubtitle()
        view.showCurrentSubtitle()
        updateViewProgress(player.calculateProgress(startIndex))
        updateSavedSubtitleIndex()
        startNWords = player.getReadWords()
        if(autoplay) startAutoplayTimer()
    }

    // stops the action of long pressing next/previous buttons
    private fun cancelContinuousTimers() {
        continuousPreviousButtonPressTimer.cancel()
        continuousNextButtonPressTimer.cancel()
    }

    // presses pause/play button
    private fun pausePlayButtonPressed() {
        if(player.isPlaying || autoplayIsActive)
            pauseSubtitleReader()
        else if(subtitleReaderIsFinished()
            && currentSubtitle.isNotEmpty()) {
            // runs when play is pressed
            // after all subtitles finished
            restartSubtitleReader()
            updateSavedSubtitleIndex()
        } else if(currentSubtitleIndex != subtitles.size
            || currentSubtitle.isNotEmpty()) {
            view.showPauseButton()
            player.start()
            startHideUITimer()
            resumeProgress()
        }
    }

    // methods for when reader is paused
    private fun pauseSubtitleReader(showPausedState: Boolean = true) {
        if(showPausedState) {
            autoplayTimer.cancel()
            autoplayIsActive = false
            view.showPlayButton()
            view.showUI()
        }
        player.pause()
        hideUITimer.cancel()
        cancelContinuousTimers()
        updateViewProgress(player.setProgress(startNWords.toDouble()))
    }

    // updates the seek bar and current time on the view
    private fun updateViewProgress(progress: Int) {
        view.updateSeekBar(progress)
        view.updateCurrentTime(getCurrentTime(progress))
    }

    // returns the current time based on the progress
    private fun getCurrentTime(progress: Int): String {
        val progressPercent = progress.toDouble() / 1000.0
        val currentMilliseconds = progressPercent * player.getTotalMilliseconds()
        return getTimeString(currentMilliseconds.toInt())
    }

    // returns if subtitle reader is finished
    private fun subtitleReaderIsFinished(): Boolean {
        return currentSubtitleIndex == subtitles.size
    }

    // if the reader is playing, a timer for 5 seconds starts
    // to hide the UI (play, previous, next buttons, etc.)
    private fun startHideUITimer(showUI: Boolean = false) {
        if(showUI) view.showUI()
        hideUITimer.cancel()
        hideUITimer = Timer()
        hideUITimer.schedule(object: TimerTask() {
            override fun run() {
                handler.post { view.hideUI() }
            }
        }, hideUITimerDuration)
    }

    // moves the progress of the subtitle reader:
    // every tenth of a second, the seek bar moves;
    // every second, the current time changes
    private fun resumeProgress() {
        val nWordsInSubtitle = player.getNWordsInSubtitle()
        val subtitleDuration = player.getSubtitleDuration()
        val wordsPerSecond =
            nWordsInSubtitle.toDouble() / (subtitleDuration.toDouble() / 1000.0)
        val recentIndex = currentSubtitleIndex
        startNWords = player.getReadWords()
        endNWords = player.getReadWords() + nWordsInSubtitle

        val timer = object : CountDownTimer(subtitleDuration, 100) {
            override fun onTick(millisUntilFinished: Long) {
                if(player.isPlaying && recentIndex == currentSubtitleIndex)
                    updateViewProgress(player.getProgress(wordsPerSecond * .1))
                else cancel()
            }
            override fun onFinish() { }
        }
        timer.start()
    }

    // listener for menu options
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.editThisSubtitle -> {
                if(subtitleReaderIsFinished()) moveToLastSubtitleFromFinished()
                view.showSubtitleEditor(subtitles[currentSubtitleIndex])
                true
            }
            R.id.removeThisSubtitle -> {
                view.showConfirmRemoveSubtitleMessage()
                true
            }
            R.id.openSourceLink -> {
                openSourceLink()
                true
            }
            R.id.editEntryText -> {
                openEditEntryText()
                true
            }
            else -> false
        }
    }

    // from the finished state, moves to the last subtitle
    private fun moveToLastSubtitleFromFinished() {
        previousButtonPressed()
        nextButtonPressed()
        if(subtitleReaderIsFinished())
            previousButtonPressed()
    }

    // saves the updated subtitles
    fun editCurrentSubtitle(editedSubtitle: String) {
        if(editedSubtitle == subtitles[currentSubtitleIndex]) return
        val indexToSkipTo = currentSubtitleIndex
        subtitles[currentSubtitleIndex] = editedSubtitle
        manager.updateSubtitles(subtitleSet, subtitles)
        reloadSubtitleReader()
        skipToSubtitle(indexToSkipTo)
    }

    // updates subtitles when current subtitle is removed
    fun removeCurrentSubtitle() {
        if(subtitleReaderIsFinished()) moveToLastSubtitleFromFinished()
        val indexOfSubtitleToRemove = currentSubtitleIndex
        currentSubtitle = ""
        nextButtonPressed()
        if(subtitleReaderIsFinished() && subtitles.size != 1)
            previousButtonPressed()
        var indexToSkipTo = currentSubtitleIndex
        if(indexToSkipTo > indexOfSubtitleToRemove) indexToSkipTo--
        subtitles.removeAt(indexOfSubtitleToRemove)
        if(subtitles.size == 0) subtitles.add("")
        manager.updateSubtitles(subtitleSet, subtitles)
        reloadSubtitleReader()
        if(indexOfSubtitleToRemove != indexToSkipTo
            || currentSubtitle.isNotEmpty())
            skipToSubtitle(indexToSkipTo)
    }

    // opens the source link of the subtitles
    private fun openSourceLink() {
        val openWebsite = Intent(Intent.ACTION_VIEW, Uri.parse(subtitleSet.sourceLink))
        openWebsite.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            startActivity(openWebsite)
        } catch (e: ActivityNotFoundException) {
            intent.setPackage(null)
            startActivity(openWebsite)
        }
    }

    // opens a page to edit the saved entry text
    private fun openEditEntryText() {
        val subtitleListPage = Intent(this, EditSubtitlesActivity::class.java)
        subtitleListPage.putExtra(idRef, subtitleSet.id)
        startActivityForResult(subtitleListPage, requestCode)
    }

    // updates the subtitle reader if the data was changed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(this.requestCode == requestCode) {
            val startIndex = data?.getIntExtra(subtitlesStartRef, -1)!!
            val subtitlesUpdated =
                data.getBooleanExtra(subtitlesUpdatedRef, false)
            val autoplay = data.getBooleanExtra(autoplayRef, false)
            if(subtitlesUpdated) reloadSubtitleReader()
            if(startIndex != -1) skipToSubtitle(startIndex, autoplay)
        }
    }

    // reloads subtitle reader with updated subtitle set
    private fun reloadSubtitleReader() {
        subtitleSet = manager.getSubtitleSet(subtitleSet.id.toString())
        subtitles = subtitleSet.subtitles
        player.updateTotalWords()
        player.updateTotalTime()
        updateUITotalTime()
        restartSubtitleReader()
        pauseSubtitleReader()
        updateSavedSubtitleIndex()
    }

    fun updateTextColor(textColor: Int) {
        subtitleSet.textColor = textColor
        manager.updateSubtitleSet(subtitleSet)
    }

    fun updateFontStyle(fontStyle: Int) {
        subtitleSet.fontStyle = fontStyle
        manager.updateSubtitleSet(subtitleSet)
    }

    fun updateSpeed(speed: String) {
        val startIndex = currentSubtitleIndex
        subtitleSet.speed = speed
        manager.updateSubtitleSet(subtitleSet)
        player.updateSpeed()
        updateUITotalTime()
        restartSubtitleReader()
        pauseSubtitleReader()
        skipToSubtitle(startIndex)
    }

    fun updateBackground(backgroundColor: Int) {
        subtitleSet.backgroundColor = backgroundColor
        manager.updateSubtitleSet(subtitleSet)
    }

    fun updateTextToSpeech(textToSpeechEnabled: Boolean) {
        subtitleSet.textToSpeechEnabled = textToSpeechEnabled
        manager.updateSubtitleSet(subtitleSet)
    }

    fun updateMaxWords(maxWordsPerSubtitle: Int) {
        subtitleSet.maxWordsPerSubtitle = maxWordsPerSubtitle
        subtitleSet.subtitles = manager.generateSubtitles(
            subtitleSet.enteredText, maxWordsPerSubtitle)
        subtitles = subtitleSet.subtitles
        manager.updateSubtitleSet(subtitleSet)
        player.updateTotalTime()
        updateUITotalTime()
        restartSubtitleReader()
        pauseSubtitleReader()
        updateSavedSubtitleIndex()
    }

    // called by player when player is finished with all subtitles
    fun finishSubtitleReader() {
        view.showUI()
        view.showPlayButton()
        currentSubtitleIndex = subtitles.size
    }

    // called by player when previous subtitle is finished playing
    fun moveToNextSubtitle() {
        if(player.isPlaying && player.hasNextSubtitle()) {
            player.start()
            view.showCurrentSubtitle()
            updateViewProgress(player.setProgress(endNWords.toDouble()))
            updateSavedSubtitleIndex()
            resumeProgress()
        } else {
            view.showPlayButton()
            updateViewProgress(1000)
            updateSavedSubtitleIndex()
            hideUITimer.cancel()
            startNWords = player.getTotalWords() - player.getNWordsInSubtitle()
        }
    }

    // called by player if text to speech is not functional
    fun disableTextToSpeech() {
        Toast.makeText(this,
            resources.getString(R.string.textToSpeechErrorMessage),
            Toast.LENGTH_LONG).show()
        pauseSubtitleReader()
        if(subtitleSet.textToSpeechEnabled) {
            subtitleSet.textToSpeechEnabled = false
            manager.updateSubtitleSet(subtitleSet)
        }
    }

    // when this activity finishes, it will send back to
    // the main activity whether or not the database was updated
    private fun initFinishProcess(backPressed: Boolean = false) {
        val data = Intent()
        data.putExtra(dbUpdatedRef, true)
        setResult(RESULT_OK, data)
        if(!backPressed) finish()
    }

    override fun onBackPressed() {
        if(player.isPlaying) pauseSubtitleReader()
        else {
            initFinishProcess(true)
            super.onBackPressed()
        }
    }

    override fun onPause() {
        cancelContinuousTimers()
        view.showPlayButton()
        player.pause()
        super.onPause()
    }

    override fun onDestroy() {
        player.shutdownTextToSpeech()
        super.onDestroy()
    }
}