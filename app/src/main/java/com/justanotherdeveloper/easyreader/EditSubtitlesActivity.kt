package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_edit_subtitles.*

@SuppressLint("InflateParams")
class EditSubtitlesActivity : AppCompatActivity() {

    private lateinit var manager: SubtitleSetManager
    private lateinit var subtitleSet: SubtitleSetManager.SubtitleSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_subtitles)
        val id = intent.getIntExtra(idRef, 0)
        manager = SubtitleSetManager(this)
        subtitleSet = manager.getSubtitleSet(id.toString())
        fillTextFields()
        initListeners()
    }

    // fills text fields with currently saved title, text, and link
    private fun fillTextFields(){
        enteredTitle.setText(subtitleSet.enteredTitle)
        enteredText.setText(subtitleSet.enteredText)
        sourceLink.setText(subtitleSet.sourceLink)
    }

    private fun initListeners() {
        backArrow.setOnClickListener {
            if(contentUpdated()) showConfirmExitMessage()
            else initFinishProcess(false)
        }

        saveButton.setOnClickListener {
            if(enteredText.text.toString() == "") {
                errorMessage.visibility = View.VISIBLE
                enteredText.requestFocus()
            } else {
                if(contentUpdated()) {
                    val startIndex = getStartIndex()
                    updateSubtitleSet()
                    initFinishProcess(true, startIndex = startIndex)
                } else initFinishProcess(false)
            }
        }
    }

    // returns if content was updated
    private fun contentUpdated(): Boolean {
        return enteredTitle.text.toString() != subtitleSet.enteredTitle ||
                enteredText.text.toString() != subtitleSet.enteredText ||
                sourceLink.text.toString() != subtitleSet.sourceLink
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
            initFinishProcess(false)
        }

        noButton.setOnClickListener {
            buttonPressed()
        }

        confirmExitMessageDialog.show()
    }

    // returns index to continue from when returning to subtitle reader
    private fun getStartIndex(): Int {
        return if(enteredText.text.toString() == subtitleSet.enteredText
            && subtitleSet.savedSubtitleIndex != subtitleSet.subtitles.size)
            subtitleSet.savedSubtitleIndex else -1
    }

    // updates the changed content
    private fun updateSubtitleSet() {
        if(enteredTitle.text.toString() != subtitleSet.enteredTitle)
            subtitleSet.enteredTitle = enteredTitle.text.toString()
        if(enteredText.text.toString() != subtitleSet.enteredText) {
            subtitleSet.enteredText = enteredText.text.toString()
            subtitleSet.subtitles = manager.generateSubtitles(
                enteredText.text.toString(), subtitleSet.maxWordsPerSubtitle) }
        if(sourceLink.text.toString() != subtitleSet.sourceLink)
            subtitleSet.sourceLink = verifyLink()
        manager.updateSubtitleSet(subtitleSet)
    }

    // returns a link that starts with http:// or https:// unless link is empty
    private fun verifyLink(): String {
        val link = sourceLink.text.toString()
        if(link.isEmpty()) return ""
        return if(!link.startsWith(webText) && !link.startsWith(webTextSecure))
            "$webTextSecure$link" else link
    }

    // when this activity finishes, it will send back to
    // the main activity whether or not the database was updated
    private fun initFinishProcess(subtitlesUpdated: Boolean, backPressed: Boolean = false,
                                  startIndex: Int = -1) {
        val data = Intent()
        data.putExtra(subtitlesUpdatedRef, subtitlesUpdated)
        data.putExtra(subtitlesStartRef, startIndex)
        setResult(RESULT_OK, data)
        if(!backPressed) finish()
    }

    override fun onBackPressed() {
        if(contentUpdated()) showConfirmExitMessage()
        else {
            initFinishProcess(subtitlesUpdated = false, backPressed = true)
            super.onBackPressed()
        }
    }
}
