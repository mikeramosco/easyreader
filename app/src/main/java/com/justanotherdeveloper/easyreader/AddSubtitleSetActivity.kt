package com.justanotherdeveloper.easyreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_subtitle_set.*

class AddSubtitleSetActivity : AppCompatActivity() {

    private lateinit var view: AddSubtitleSetViewMethods
    private lateinit var manager: SubtitleSetManager
    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subtitle_set)
        view = AddSubtitleSetViewMethods(this)
        manager = SubtitleSetManager(this)
        addTitleParent.requestFocus()
        initListeners()
    }

    private fun initListeners() {

        // setting buttons
        backgroundSetting.setOnClickListener { view.showBackgroundSettings() }
        fontSetting.setOnClickListener { view.showFontSettings() }
        speedSetting.setOnClickListener { view.showSpeedSettings() }
        textColorSetting.setOnClickListener { view.showTextColorSettings() }
        textToSpeechSetting.setOnClickListener { view.switchTextToSpeechSetting() }
        maxWordsSetting.setOnClickListener { view.showMaxWordsSettingEntry() }
        hideOrShowSettingsLayout.setOnClickListener { view.hideOrShowSettings() }

        backArrow.setOnClickListener {
            if(contentAdded()) view.showConfirmExitMessage()
            else initFinishProcess(false)
        }

        saveButton.setOnClickListener {
            if(enteredText.text.toString() == "") view.showSaveFailedMessage()
            else {
                manager.saveAddedSubtitleSet(view.textColor, view.fontStyle, view.speed,
                    view.backgroundColor, view.textToSpeechEnabled, view.maxWordsPerSubtitle,
                    verifyLink(), enteredTitle.text.toString(), enteredText.text.toString())

                initFinishProcess(true)
            }
        }

        startReadingButton.setOnClickListener {
            if(enteredText.text.toString() == "") view.showSaveFailedMessage()
            else {
                val id = manager.saveAddedSubtitleSet(view.textColor, view.fontStyle, view.speed,
                    view.backgroundColor, view.textToSpeechEnabled, view.maxWordsPerSubtitle,
                    verifyLink(), enteredTitle.text.toString(), enteredText.text.toString())

                val subtitleReaderPage = Intent(this, SubtitleReaderActivity::class.java)
                subtitleReaderPage.putExtra(idRef, id)
                startActivityForResult(subtitleReaderPage, requestCode)
            }
        }
    }

    // returns a link that starts with http:// or https:// unless link is empty
    private fun verifyLink(): String {
        val link = sourceLink.text.toString()
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
        return enteredTitle.text.toString() != "" || enteredText.text.toString() != ""
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
        if(!addTitleParent.isFocused) addTitleParent.requestFocus()
        else if(contentAdded()) view.showConfirmExitMessage()
        else {
            initFinishProcess(dbUpdated = false, backPressed = true)
            super.onBackPressed()
        }
    }
}
