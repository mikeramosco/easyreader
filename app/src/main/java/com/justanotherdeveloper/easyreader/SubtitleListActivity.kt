package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_subtitle_list.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams")
class SubtitleListActivity : AppCompatActivity() {

    private lateinit var manager: SubtitleSetManager
    private lateinit var subtitleSet: SubtitleSetManager.SubtitleSet
    private lateinit var subtitles: ArrayList<String>

    private val modifiedSubtitles = ArrayList<String>()
    private val subtitleViews = ArrayList<View>()
    private val buttons = ArrayList<ImageView>()
    private var subtitlesUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtitle_list)
        val id = intent.getIntExtra(idRef, 0)
        manager = SubtitleSetManager(this)
        subtitleSet = manager.getSubtitleSet(id.toString())
        subtitles = subtitleSet.subtitles
        displaySubtitles()
        initListeners()
    }

    // loops through & displays each subtitle
    private fun displaySubtitles() {
        for((index, text) in subtitles.withIndex()) {
            val subtitleView = layoutInflater.inflate(R.layout.widget_subtitle, null)
            val subtitleTextView =
                subtitleView.findViewById<TextView>(R.id.subtitleTextView)
            val removeButton = subtitleView.findViewById<ImageView>(R.id.removeButton)
            val playButton = subtitleView.findViewById<ImageView>(R.id.playButton)
            highlightIfCurrentSubtitle(index, subtitleTextView)
            buttons.add(removeButton)
            buttons.add(playButton)

            fun subtitleSelected(autoplay: Boolean) {
                for(button in buttons) button.isEnabled = false
                updateSubtitles()
                initFinishProcess(subtitlesContainer
                    .indexOfChild(subtitleView), autoplay = autoplay)
            }

            val subtitle = trimTextSpaces(text)
            modifiedSubtitles.add(subtitle)
            if(subtitle.isEmpty()) subtitleView.visibility = View.GONE
            subtitleTextView.text = subtitle
            removeButton.setOnClickListener {
                subtitlesUpdated = true
                beginTransition(subtitlesContainer)
                val indexOfView = subtitlesContainer.indexOfChild(subtitleView)
                subtitlesContainer.removeViewAt(indexOfView)
                modifiedSubtitles.removeAt(indexOfView)
                subtitleViews.removeAt(indexOfView)
                subtitles.removeAt(indexOfView) }
            playButton.setOnClickListener { subtitleSelected(true) }
            subtitleView.setOnClickListener { subtitleSelected(false) }
            subtitlesContainer.addView(subtitleView)
            subtitleViews.add(subtitleView)
        }
    }

    // highlights subtitle if it is current subtitle
    private fun highlightIfCurrentSubtitle(index: Int, subtitleTextView: TextView) {
        if(index == 0) if(subtitleSet.savedSubtitleIndex == -1
            || subtitleSet.savedSubtitleIndex == subtitles.size)
            subtitleTextView.setTextColor(ContextCompat
                .getColor(this, R.color.colorPrimary))
        if(index == subtitleSet.savedSubtitleIndex)
            subtitleTextView.setTextColor(ContextCompat
                .getColor(this, R.color.colorPrimary))
    }

    // saves updated subtitles
    private fun updateSubtitles() {
        if(subtitles.isEmpty()) subtitles.add("")
        if(subtitlesUpdated) manager.updateSubtitles(subtitleSet, subtitles)
    }

    private fun initListeners() {
        saveButton.setOnClickListener {
            updateSubtitles()
            initFinishProcess()
        }

        backArrow.setOnClickListener {
            if(subtitlesUpdated) showConfirmExitMessage()
            else initFinishProcess()
        }

        searchButton.setOnClickListener {
            if(searchButton.text == resources.getString(R.string.searchString)) {
                searchButton.text = resources.getString(R.string.cancelString)
                beginTransition(subtitleListParent)
                searchBar.visibility = View.VISIBLE
                searchBar.requestFocus()
            } else {
                searchButton.text = resources.getString(R.string.searchString)
                searchBar.setText("")
                subtitleListParent.requestFocus()
                searchBar.post{
                    beginTransition(subtitleListParent)
                    searchBar.visibility = View.GONE
                }
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val wordsToSearch = parseWordsFromSearchBar()
                if(wordsToSearch.size == 0) showAllSubtitleViews()
                else for((index, subtitle) in subtitles.withIndex()) {
                    var hideView = false
                    for(word in wordsToSearch)
                        if(!subtitle.toLowerCase(Locale.US)
                                .contains(word.toLowerCase(Locale.US)))
                            hideView = true
                    subtitleViews[index].visibility =
                        if(hideView) View.GONE else View.VISIBLE
                }
            }
        })
    }

    // shows confirm exit message on a bottomsheet dialog
    private fun showConfirmExitMessage() {
        val confirmExitMessageDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_message_dialog, null)
        confirmExitMessageDialog.setContentView(view)

        val yesButton = view.findViewById<Button>(R.id.yesButton)
        val noButton = view.findViewById<Button>(R.id.noButton)

        fun buttonPressed() {
            yesButton.isEnabled = false
            noButton.isEnabled = false
            confirmExitMessageDialog.dismiss()
        }

        yesButton.setOnClickListener {
            buttonPressed()
            subtitlesUpdated = false
            initFinishProcess()
        }

        noButton.setOnClickListener {
            buttonPressed()
        }

        confirmExitMessageDialog.show()
    }

    private fun parseWordsFromSearchBar(): ArrayList<String> {
        val wordsToSearch = ArrayList<String>()
        val searchText = searchBar.text.toString()
        var nextWord = ""
        for(ch in searchText) {
            if(ch != ' ') nextWord += ch
            else {
                if(nextWord != "") wordsToSearch.add(nextWord)
                nextWord = ""
            }
        }
        if(nextWord != "") wordsToSearch.add(nextWord)
        return wordsToSearch
    }

    private fun showAllSubtitleViews() {
        for((index, view) in subtitleViews.withIndex())
            if(modifiedSubtitles[index].isNotEmpty()) view.visibility = View.VISIBLE
    }

    // when this activity finishes, it will send back to the main activity
    private fun initFinishProcess(startIndex: Int = -1, backPressed: Boolean = false,
                                  autoplay: Boolean = false) {
        val data = Intent()
        data.putExtra(subtitlesUpdatedRef, subtitlesUpdated)
        data.putExtra(subtitlesStartRef, startIndex)
        data.putExtra(autoplayRef, autoplay)
        setResult(RESULT_OK, data)
        if(!backPressed) finish()
    }

    override fun onBackPressed() {
        if(subtitlesUpdated) showConfirmExitMessage()
        else {
            initFinishProcess(backPressed = true)
            super.onBackPressed()
        }
    }
}
