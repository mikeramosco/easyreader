package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.justanotherdeveloper.easyreader.databinding.ActivityEditSubtitlesBinding

@SuppressLint("InflateParams")
class EditSubtitlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSubtitlesBinding

    private lateinit var manager: SubtitleSetManager
    private lateinit var subtitleSet: SubtitleSetManager.SubtitleSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        val id = intent.getIntExtra(idRef, 0)
        manager = SubtitleSetManager(this)
        subtitleSet = manager.getSubtitleSet(id.toString())
        fillTextFields()
        initListeners()
    }

    private fun initBinding() {
        binding = ActivityEditSubtitlesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // fills text fields with currently saved title, text, and link
    private fun fillTextFields(){
        binding.enteredTitle.setText(subtitleSet.enteredTitle)
        binding.enteredText.setText(subtitleSet.enteredText)
        binding.sourceLink.setText(subtitleSet.sourceLink)
    }

    private fun initListeners() {
        binding.backArrow.setOnClickListener {
            if(contentUpdated()) showConfirmExitMessage()
            else initFinishProcess(false)
        }

        binding.removeAllButton.setOnClickListener {
            val textToRemove = binding.removeAllField.text.toString()
            if(textToRemove.isNotEmpty()) {
                val enteredText = binding.enteredText.text.toString()
                binding.enteredText.setText(enteredText.replace(textToRemove, ""))
                binding.removeAllField.setText("")
                showToast(getString(R.string.textRemovedMessage, textToRemove))
            }
        }

        binding.saveButton.setOnClickListener {
            if(binding.enteredText.text.toString() == "") {
                binding.errorMessage.visibility = View.VISIBLE
                binding.enteredText.requestFocus()
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
        return binding.enteredTitle.text.toString() != subtitleSet.enteredTitle ||
                binding.enteredText.text.toString() != subtitleSet.enteredText ||
                binding.sourceLink.text.toString() != subtitleSet.sourceLink
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
        return if(binding.enteredText.text.toString() == subtitleSet.enteredText
            && subtitleSet.savedSubtitleIndex != subtitleSet.subtitles.size)
            subtitleSet.savedSubtitleIndex else -1
    }

    // updates the changed content
    private fun updateSubtitleSet() {
        if(binding.enteredTitle.text.toString() != subtitleSet.enteredTitle)
            subtitleSet.enteredTitle = binding.enteredTitle.text.toString()
        if(binding.enteredText.text.toString() != subtitleSet.enteredText) {
            subtitleSet.enteredText = binding.enteredText.text.toString()
            subtitleSet.subtitles = manager.generateSubtitles(
                binding.enteredText.text.toString(), subtitleSet.maxWordsPerSubtitle) }
        if(binding.sourceLink.text.toString() != subtitleSet.sourceLink)
            subtitleSet.sourceLink = verifyLink()
        manager.updateSubtitleSet(subtitleSet)
    }

    // returns a link that starts with http:// or https:// unless link is empty
    private fun verifyLink(): String {
        val link = binding.sourceLink.text.toString()
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
