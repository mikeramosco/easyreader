package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_subtitle_reader.*

// View methods class - front end management for subtitle reader activity
@SuppressLint("InflateParams")
class SubtitleReaderViewMethods(private val activity:SubtitleReaderActivity) {

    init {
        initSeekBar()
        initSubtitle()
        updateBackground(activity.subtitleSet.backgroundColor)
    }

    private fun initSeekBar() {
        activity.seekbar.progressDrawable.setColorFilter(Color.BLACK)
        activity.seekbar.thumb.setColorFilter(Color.WHITE)
        activity.seekbar.max = 1000
    }

    private fun initSubtitle() {
        updateTextColor(activity.subtitleSet.textColor)
        updateFontStyle(activity.subtitleSet.fontStyle)
    }

    private fun updateBackground(backgroundColor: Int) {
        activity.subtitleReaderParent.setBackgroundColor(backgroundColor)
    }

    private fun updateTextColor(textColor: Int) {
        activity.subtitleTextView.setTextColor(textColor)
    }

    private fun updateFontStyle(fontStyle: Int) {
        activity.subtitleTextView.setFontStyle(activity, fontStyle)
    }

    fun hideUI() {
        if(activity.UIArea.visibility == View.VISIBLE) {
            beginTransition(activity.subtitleReaderParent)
            activity.UIArea.visibility = View.GONE
            activity.toolbar.visibility = View.GONE
        }
    }

    fun showUI() {
        if(activity.UIArea.visibility == View.GONE) {
            beginTransition(activity.subtitleReaderParent)
            activity.UIArea.visibility = View.VISIBLE
            activity.toolbar.visibility = View.VISIBLE
        }
    }

    fun showCurrentSubtitle(showTransition: Boolean = true) {
        if(showTransition) beginTransition(activity.subtitleReaderParent)
        activity.subtitleTextView.text = activity.currentSubtitle
    }

    fun showPlayButton() {
        activity.pausePlayButton.setImageResource(R.drawable.ic_play)
    }

    fun showPauseButton() {
        activity.pausePlayButton.setImageResource(R.drawable.ic_pause)
    }

    fun setTotalTime(totalTime: String) {
        activity.totalTime.text = totalTime
    }

    fun updateCurrentTime(currentTime: String) {
        activity.currentTime.text = currentTime
    }

    fun updateSeekBar(progress: Int) {
        activity.seekbar.progress = progress
    }

    fun showSubtitleEditor(subtitle: String) {
        val editSubtitleDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_edit_subtitle, null)
        editSubtitleDialog.setContentView(view)

        val subtitleTextField = view.findViewById<EditText>(R.id.subtitleTextField)
        val errorMessage = view.findViewById<TextView>(R.id.errorMessage)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        subtitleTextField.setText(subtitle)

        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter.LengthFilter(maxCharPerSubtitle)
        subtitleTextField.filters = filters
        subtitleTextField.setSelection(subtitleTextField.text.toString().length)

        saveButton.setOnClickListener {
            if(subtitleTextField.text.toString() == "") {
                errorMessage.visibility = View.VISIBLE
            } else {
                activity.editCurrentSubtitle(subtitleTextField.text.toString())
                editSubtitleDialog.dismiss()
            }
        }

        editSubtitleDialog.show()
    }

    fun showConfirmRemoveSubtitleMessage() {
        val confirmExitMessageDialog = BottomSheetDialog(activity)
        val view =
            activity.layoutInflater.inflate(R.layout.bottomsheet_message_dialog, null)
        confirmExitMessageDialog.setContentView(view)

        val subtitleToRemove = if(activity.currentSubtitle.length > maxCharSubtitleToRemove)
            "${activity.currentSubtitle.substring(0, maxCharSubtitleToRemove)}..."
                .replace('\n', ' ')
        else activity.currentSubtitle.replace('\n', ' ')

        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text =
            activity.resources.getString(R.string.confirmRemoveSubtitlePrompt, subtitleToRemove)

        val yesButton = view.findViewById<Button>(R.id.yesButton)
        val noButton = view.findViewById<Button>(R.id.noButton)

        fun buttonPressed() {
            yesButton.isEnabled = false
            noButton.isEnabled = false
            confirmExitMessageDialog.dismiss()
        }

        yesButton.setOnClickListener {
            buttonPressed()
            activity.removeCurrentSubtitle()
        }

        noButton.setOnClickListener {
            buttonPressed()
        }

        confirmExitMessageDialog.show()
    }

    fun showSettings() {
        val settingsDialog = BottomSheetDialog(activity)
        val view =
            activity.layoutInflater.inflate(R.layout.bottomsheet_subtitle_reader_settings, null)
        settingsDialog.setContentView(view)

        val textColorSetting = view.findViewById<LinearLayout>(R.id.textColorSetting)
        val fontSetting = view.findViewById<LinearLayout>(R.id.fontSetting)
        val speedSetting = view.findViewById<LinearLayout>(R.id.speedSetting)
        val backgroundSetting = view.findViewById<LinearLayout>(R.id.backgroundSetting)
        val textToSpeechSetting = view.findViewById<LinearLayout>(R.id.textToSpeechSetting)
        val maxWordsSetting = view.findViewById<LinearLayout>(R.id.maxWordsSetting)

        val textColorView = view.findViewById<TextView>(R.id.textColorView)
        val fontTextView = view.findViewById<TextView>(R.id.fontTextView)
        val speedTextView = view.findViewById<TextView>(R.id.speedTextView)
        val backgroundView = view.findViewById<TextView>(R.id.backgroundView)
        val textToSpeechTextView = view.findViewById<TextView>(R.id.textToSpeechTextView)
        val maxWordsTextView = view.findViewById<TextView>(R.id.maxWordsTextView)

        speedTextView.text = activity.subtitleSet.speed
        maxWordsTextView.text = activity.subtitleSet.maxWordsPerSubtitle.toString()
        textColorView.background.setColorFilter(activity.subtitleSet.textColor)
        backgroundView.background.setColorFilter(activity.subtitleSet.backgroundColor)
        textToSpeechTextView.text = if(activity.subtitleSet.textToSpeechEnabled)
            activity.resources.getString(R.string.textToSpeechEnabled)
        else activity.resources.getString(R.string.textToSpeechDisabled)
        fontTextView.setFontStyle(activity, activity.subtitleSet.fontStyle)

        fun disableOptions() {
            textColorSetting.isEnabled = false
            fontSetting.isEnabled = false
            speedSetting.isEnabled = false
            backgroundSetting.isEnabled = false
            textToSpeechSetting.isEnabled = false
            maxWordsSetting.isEnabled = false
        }

        fun enableOptions() {
            textColorSetting.isEnabled = true
            fontSetting.isEnabled = true
            speedSetting.isEnabled = true
            backgroundSetting.isEnabled = true
            textToSpeechSetting.isEnabled = true
            maxWordsSetting.isEnabled = true
        }

        fun settingSelected( dialog: BottomSheetDialog) {
            disableOptions()
            dialog.setOnDismissListener { enableOptions() }
        }

        textColorSetting.setOnClickListener {
            settingSelected(showTextColorSettings(textColorView)) }
        fontSetting.setOnClickListener {
            settingSelected(showFontSettings(fontTextView)) }
        speedSetting.setOnClickListener {
            settingSelected(showSpeedSettings(speedTextView)) }
        backgroundSetting.setOnClickListener {
            settingSelected(showBackgroundSettings(backgroundView)) }
        textToSpeechSetting.setOnClickListener {
            switchTextToSpeechSetting(textToSpeechTextView) }
        maxWordsSetting.setOnClickListener {
            settingSelected(showMaxWordsSettingEntry(maxWordsTextView)) }

        settingsDialog.show()
    }

    private fun showTextColorSettings(textColorView: TextView): BottomSheetDialog {
        val textColorSettingsDialog = BottomSheetDialog(activity)
        val view =
            activity.layoutInflater.inflate(R.layout.bottomsheet_simple_color_picker, null)
        textColorSettingsDialog.setContentView(view)

        val prompt = view.findViewById<TextView>(R.id.colorPickerPrompt)
        prompt.text = activity.resources.getString(R.string.textColorSettingsPrompt)

        val setting1 = view.findViewById<LinearLayout>(R.id.colorSetting1)
        val setting2 = view.findViewById<LinearLayout>(R.id.colorSetting2)
        val setting3 = view.findViewById<LinearLayout>(R.id.colorSetting3)
        val setting4 = view.findViewById<LinearLayout>(R.id.colorSetting4)
        val setting5 = view.findViewById<LinearLayout>(R.id.colorSetting5)

        val colorSettingView1 = view.findViewById<TextView>(R.id.colorSettingView1)
        val colorSettingView2 = view.findViewById<TextView>(R.id.colorSettingView2)
        val colorSettingView3 = view.findViewById<TextView>(R.id.colorSettingView3)
        val colorSettingView4 = view.findViewById<TextView>(R.id.colorSettingView4)
        val colorSettingView5 = view.findViewById<TextView>(R.id.colorSettingView5)

        colorSettingView1.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColorSetting1))
        colorSettingView2.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColorSetting2))
        colorSettingView3.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColorSetting3))
        colorSettingView4.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColorSetting4))
        colorSettingView5.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColorSetting5))

        val colorSettingString1 = view.findViewById<TextView>(R.id.colorSettingString1)
        val colorSettingString2 = view.findViewById<TextView>(R.id.colorSettingString2)
        val colorSettingString3 = view.findViewById<TextView>(R.id.colorSettingString3)
        val colorSettingString4 = view.findViewById<TextView>(R.id.colorSettingString4)
        val colorSettingString5 = view.findViewById<TextView>(R.id.colorSettingString5)

        colorSettingString1.text = activity.resources.getString(R.string.textColorSetting1)
        colorSettingString2.text = activity.resources.getString(R.string.textColorSetting2)
        colorSettingString3.text = activity.resources.getString(R.string.textColorSetting3)
        colorSettingString4.text = activity.resources.getString(R.string.textColorSetting4)
        colorSettingString5.text = activity.resources.getString(R.string.textColorSetting5)

        fun disableOptions() {
            setting1.isEnabled = false
            setting2.isEnabled = false
            setting3.isEnabled = false
            setting4.isEnabled = false
            setting5.isEnabled = false
        }

        fun settingSelected(textColor: Int) {
            disableOptions()
            textColorSettingsDialog.dismiss()
            updateTextColor(textColor)
            textColorView.background.setColorFilter(textColor)
            activity.updateTextColor(textColor)
        }

        setting1.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.textColorSetting1)) }
        setting2.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.textColorSetting2)) }
        setting3.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.textColorSetting3)) }
        setting4.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.textColorSetting4)) }
        setting5.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.textColorSetting5)) }

        textColorSettingsDialog.show()

        return textColorSettingsDialog
    }

    private fun showFontSettings(fontTextView: TextView): BottomSheetDialog {
        val fontSettingsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_font_settings, null)
        fontSettingsDialog.setContentView(view)

        val setting1 = view.findViewById<LinearLayout>(R.id.fontSetting1)
        val setting2 = view.findViewById<LinearLayout>(R.id.fontSetting2)
        val setting3 = view.findViewById<LinearLayout>(R.id.fontSetting3)
        val setting4 = view.findViewById<LinearLayout>(R.id.fontSetting4)
        val setting5 = view.findViewById<LinearLayout>(R.id.fontSetting5)

        fun disableOptions() {
            setting1.isEnabled = false
            setting2.isEnabled = false
            setting3.isEnabled = false
            setting4.isEnabled = false
            setting5.isEnabled = false
        }

        fun settingSelected(fontStyle: Int) {
            disableOptions()
            fontSettingsDialog.dismiss()
            fontTextView.setFontStyle(activity, fontStyle)
            updateFontStyle(fontStyle)
            activity.updateFontStyle(fontStyle)
        }

        setting1.setOnClickListener { settingSelected(R.style.fontSetting1) }
        setting2.setOnClickListener { settingSelected(R.style.fontSetting2) }
        setting3.setOnClickListener { settingSelected(R.style.fontSetting3) }
        setting4.setOnClickListener { settingSelected(R.style.fontSetting4) }
        setting5.setOnClickListener { settingSelected(R.style.fontSetting5) }

        fontSettingsDialog.show()

        return fontSettingsDialog
    }

    private fun showSpeedSettings(speedTextView: TextView): BottomSheetDialog {
        val speedSettingsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_speed_settings, null)
        speedSettingsDialog.setContentView(view)

        val setting1 = view.findViewById<LinearLayout>(R.id.speedSetting1)
        val setting2 = view.findViewById<LinearLayout>(R.id.speedSetting2)
        val setting3 = view.findViewById<LinearLayout>(R.id.speedSetting3)
        val setting4 = view.findViewById<LinearLayout>(R.id.speedSetting4)
        val setting5 = view.findViewById<LinearLayout>(R.id.speedSetting5)

        fun disableOptions() {
            setting1.isEnabled = false
            setting2.isEnabled = false
            setting3.isEnabled = false
            setting4.isEnabled = false
            setting5.isEnabled = false
        }

        fun settingSelected(speed: String) {
            disableOptions()
            speedSettingsDialog.dismiss()
            speedTextView.text = speed
            activity.updateSpeed(speed)
        }

        setting1.setOnClickListener {
            settingSelected(activity.resources.getString(R.string.speedSetting1)) }
        setting2.setOnClickListener {
            settingSelected(activity.resources.getString(R.string.speedSetting2)) }
        setting3.setOnClickListener {
            settingSelected(activity.resources.getString(R.string.speedSetting3)) }
        setting4.setOnClickListener {
            settingSelected(activity.resources.getString(R.string.speedSetting4)) }
        setting5.setOnClickListener {
            settingSelected(activity.resources.getString(R.string.speedSetting5)) }

        speedSettingsDialog.show()

        return speedSettingsDialog
    }

    private fun showBackgroundSettings(backgroundView: TextView): BottomSheetDialog {
        val backgroundSettingsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_simple_color_picker, null)
        backgroundSettingsDialog.setContentView(view)

        val setting1 = view.findViewById<LinearLayout>(R.id.colorSetting1)
        val setting2 = view.findViewById<LinearLayout>(R.id.colorSetting2)
        val setting3 = view.findViewById<LinearLayout>(R.id.colorSetting3)
        val setting4 = view.findViewById<LinearLayout>(R.id.colorSetting4)
        val setting5 = view.findViewById<LinearLayout>(R.id.colorSetting5)

        val colorSettingView1 = view.findViewById<TextView>(R.id.colorSettingView1)
        val colorSettingView2 = view.findViewById<TextView>(R.id.colorSettingView2)
        val colorSettingView3 = view.findViewById<TextView>(R.id.colorSettingView3)
        val colorSettingView4 = view.findViewById<TextView>(R.id.colorSettingView4)
        val colorSettingView5 = view.findViewById<TextView>(R.id.colorSettingView5)

        colorSettingView1.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.backgroundSetting1))
        colorSettingView2.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.backgroundSetting2))
        colorSettingView3.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.backgroundSetting3))
        colorSettingView4.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.backgroundSetting4))
        colorSettingView5.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.backgroundSetting5))

        fun disableOptions() {
            setting1.isEnabled = false
            setting2.isEnabled = false
            setting3.isEnabled = false
            setting4.isEnabled = false
            setting5.isEnabled = false
        }

        fun settingSelected(backgroundColor: Int) {
            disableOptions()
            backgroundSettingsDialog.dismiss()
            backgroundView.background.setColorFilter(backgroundColor)
            updateBackground(backgroundColor)
            activity.updateBackground(backgroundColor)
        }

        setting1.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.backgroundSetting1)) }
        setting2.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.backgroundSetting2)) }
        setting3.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.backgroundSetting3)) }
        setting4.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.backgroundSetting4)) }
        setting5.setOnClickListener {
            settingSelected(ContextCompat.getColor(activity, R.color.backgroundSetting5)) }

        backgroundSettingsDialog.show()

        return backgroundSettingsDialog
    }

    private fun switchTextToSpeechSetting(textToSpeechTextView: TextView) {
        textToSpeechTextView.text = if(activity.subtitleSet.textToSpeechEnabled) {
            activity.updateTextToSpeech(false)
            activity.resources.getString(R.string.textToSpeechDisabled)
        } else {
            activity.updateTextToSpeech(true)
            activity.resources.getString(R.string.textToSpeechEnabled)
        }
    }

    private fun showMaxWordsSettingEntry(maxWordsTextView: TextView): BottomSheetDialog {
        val maxWordsSettingEntryDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_max_words_setting, null)
        maxWordsSettingEntryDialog.setContentView(view)

        val prompt = view.findViewById<TextView>(R.id.maxWordsPrompt)
        val errorMessage = view.findViewById<TextView>(R.id.errorMessage)

        prompt.text = activity.resources.getString(
            R.string.maxWordsSettingPrompt, minEnteredWordsPerSubtitle, maxEnteredWordsPerSubtitle)
        errorMessage.text = activity.resources.getString(
            R.string.maxWordsInvalidMessage, minEnteredWordsPerSubtitle, maxEnteredWordsPerSubtitle)

        val maxWordsSettingEntry = view.findViewById<EditText>(R.id.enteredMaxWords)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            if(maxWordsSettingEntry.text.toString() == "") {
                errorMessage.visibility = View.VISIBLE
            } else {
                val maxWordsEntered = maxWordsSettingEntry.text.toString().toInt()
                if (maxWordsEntered < minEnteredWordsPerSubtitle ||
                    maxWordsEntered > maxEnteredWordsPerSubtitle) {
                    errorMessage.visibility = View.VISIBLE
                } else {
                    saveButton.isEnabled = false
                    maxWordsSettingEntryDialog.dismiss()
                    maxWordsTextView.text = maxWordsEntered.toString()
                    activity.updateMaxWords(maxWordsEntered)
                }
            }
        }

        maxWordsSettingEntryDialog.show()

        return maxWordsSettingEntryDialog
    }
}