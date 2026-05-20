package com.justanotherdeveloper.easyreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.justanotherdeveloper.easyreader.databinding.ActivityAddSubtitleSetBinding

class AddSubtitleSetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSubtitleSetBinding

    private lateinit var view: AddSubtitleSetViewMethods
    private lateinit var manager: SubtitleSetManager
    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        view = AddSubtitleSetViewMethods(this)
        manager = SubtitleSetManager(this)
        binding.addTitleParent.requestFocus()
        initListeners()
    }

    private fun initBinding() {
        binding = ActivityAddSubtitleSetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun getBinding(): ActivityAddSubtitleSetBinding {
        return binding
    }

    private fun initListeners() {

        // setting buttons
        binding.backgroundSetting.setOnClickListener { view.showBackgroundSettings() }
        binding.fontSetting.setOnClickListener { view.showFontSettings() }
        binding.speedSetting.setOnClickListener { view.showSpeedSettings() }
        binding.textColorSetting.setOnClickListener { view.showTextColorSettings() }
        binding.textToSpeechSetting.setOnClickListener { view.switchTextToSpeechSetting() }
        binding.maxWordsSetting.setOnClickListener { view.showMaxWordsSettingEntry() }
        binding.hideOrShowSettingsLayout.setOnClickListener { view.hideOrShowSettings() }

        binding.backArrow.setOnClickListener {
            if(contentAdded()) view.showConfirmExitMessage()
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
            if(binding.enteredText.text.toString() == "") view.showSaveFailedMessage()
            else {
                manager.saveAddedSubtitleSet(view.textColor, view.fontStyle, view.speed,
                    view.backgroundColor, view.textToSpeechEnabled, view.maxWordsPerSubtitle,
                    verifyLink(), binding.enteredTitle.text.toString(), binding.enteredText.text.toString())

                initFinishProcess(true)
            }
        }

        binding.startReadingButton.setOnClickListener {
            if(binding.enteredText.text.toString() == "") view.showSaveFailedMessage()
            else {
                val id = manager.saveAddedSubtitleSet(view.textColor, view.fontStyle, view.speed,
                    view.backgroundColor, view.textToSpeechEnabled, view.maxWordsPerSubtitle,
                    verifyLink(), binding.enteredTitle.text.toString(), binding.enteredText.text.toString())

                val subtitleReaderPage = Intent(this, SubtitleReaderActivity::class.java)
                subtitleReaderPage.putExtra(idRef, id)
                startActivityForResult(subtitleReaderPage, requestCode)
            }
        }
    }

    // returns a link that starts with http:// or https:// unless link is empty
    private fun verifyLink(): String {
        val link = binding.sourceLink.text.toString()
        if(link.isEmpty()) return ""
        return if(!link.startsWith(webText) && !link.startsWith(webTextSecure))
            "$webTextSecure$link" else link
    }

    // when subtitle reader finishes and returns to this activity,
    // it automatically finishes as well
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(this.requestCode == requestCode) initFinishProcess(true)
    }

    // checks if any content has been added by the user
    private fun contentAdded(): Boolean {
        return binding.enteredTitle.text.toString() != ""
                || binding.enteredText.text.toString() != ""
    }

    // when this activity finishes, it will send back to
    // the main activity whether or not the database was updated
    fun initFinishProcess(dbUpdated: Boolean, backPressed: Boolean = false) {
        val data = Intent()
        data.putExtra(dbUpdatedRef, dbUpdated)
        setResult(RESULT_OK, data)
        if(!backPressed) finish()
    }

    override fun onBackPressed() {
        if(!binding.addTitleParent.isFocused) binding.addTitleParent.requestFocus()
        else if(contentAdded()) view.showConfirmExitMessage()
        else {
            initFinishProcess(dbUpdated = false, backPressed = true)
            super.onBackPressed()
        }
    }
}
