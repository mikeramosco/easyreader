package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

@SuppressLint("InflateParams")
class AddSubtitleSetViewMethods(private val activity: AddSubtitleSetActivity) {

    private val binding = activity.getBinding()

    var speed: String
    var fontStyle = R.style.fontSetting3
    var textToSpeechEnabled = true
    var textColor = 0
    var backgroundColor = 0
    var maxWordsPerSubtitle = 0

    init {
        speed = activity.resources.getString(R.string.defaultSpeed)
        maxWordsPerSubtitle = activity.resources.getString(R.string.defaultMaxWords).toInt()
        setDefaults()
    }

    // sets default background & text colors
    private fun setDefaults() {
        binding.textColorView.background.setColorFilter(Color.BLACK)
        binding.backgroundView.background.setColorFilter(
            ContextCompat.getColor(activity, R.color.colorPrimary))
        textColor = Color.BLACK
        backgroundColor = ContextCompat.getColor(activity, R.color.colorPrimary)
    }

    // hides or shows settings
    fun hideOrShowSettings() {
        beginTransition(binding.addTitleParent)
        if(binding.settingsLayout.visibility == View.GONE) {
            binding.settingsLayout.visibility = View.VISIBLE
            binding.showSettingsTextView.visibility = View.GONE
            binding.hideSettingsTextView.visibility = View.VISIBLE
            binding.settingsArrowImage.setImageResource(R.drawable.ic_arrow_down)
        } else {
            binding.settingsLayout.visibility = View.GONE
            binding.showSettingsTextView.visibility = View.VISIBLE
            binding.hideSettingsTextView.visibility = View.GONE
            binding.settingsArrowImage.setImageResource(R.drawable.ic_arrow_up)
        }
    }

    // shows background settings on bottomsheet dialog
    fun showBackgroundSettings() {
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
            this.backgroundColor = backgroundColor
            disableOptions()
            backgroundSettingsDialog.dismiss()
            binding.backgroundView.background.setColorFilter(backgroundColor)
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
    }

    // shows font settings on a bottomsheet dialog
    fun showFontSettings() {
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
            this.fontStyle = fontStyle
            disableOptions()
            fontSettingsDialog.dismiss()
            binding.fontTextView.setFontStyle(activity, fontStyle)
        }

        setting1.setOnClickListener { settingSelected(R.style.fontSetting1) }
        setting2.setOnClickListener { settingSelected(R.style.fontSetting2) }
        setting3.setOnClickListener { settingSelected(R.style.fontSetting3) }
        setting4.setOnClickListener { settingSelected(R.style.fontSetting4) }
        setting5.setOnClickListener { settingSelected(R.style.fontSetting5) }

        fontSettingsDialog.show()
    }

    // shows speed settings on a bottomsheet dialog
    fun showSpeedSettings() {
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
            this.speed = speed
            disableOptions()
            speedSettingsDialog.dismiss()
            binding.speedTextView.text = speed
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
    }

    // shows background settings on bottomsheet dialog
    fun showTextColorSettings() {
        val textColorSettingsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_simple_color_picker, null)
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
            this.textColor = textColor
            disableOptions()
            textColorSettingsDialog.dismiss()
            binding.textColorView.background.setColorFilter(textColor)
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
    }

    // switches text-to-speech setting on or off
    fun switchTextToSpeechSetting() {
        textToSpeechEnabled = !textToSpeechEnabled
        binding.textToSpeechTextView.text = if(textToSpeechEnabled)
            activity.resources.getString(R.string.textToSpeechEnabled)
        else activity.resources.getString(R.string.textToSpeechDisabled)
    }

    // shows max words setting entry on bottomsheet dialog
    fun showMaxWordsSettingEntry() {
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
        val recommendedOption = view.findViewById<TextView>(R.id.recommendedOption)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        recommendedOption.setOnClickListener {
            val enteredText = binding.enteredText.text.toString()
            if(enteredText.isNotEmpty())
                activity.calculateRecommended(enteredText, maxWordsSettingEntry)
            else maxWordsSettingEntry.setText(
                activity.getString(R.string.defaultMaxWords))
        }

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
                    maxWordsPerSubtitle = maxWordsEntered
                    maxWordsSettingEntryDialog.dismiss()
                    binding.maxWordsTextView.text = maxWordsPerSubtitle.toString()
                }
            }
        }

        maxWordsSettingEntryDialog.show()
    }

    // error message shown if no text entered when saving attempted
    fun showSaveFailedMessage() {
        binding.errorMessage.visibility = View.VISIBLE
        binding.enteredText.requestFocus()
    }

    // shows confirm exit message on a bottomsheet dialog
    fun showConfirmExitMessage() {
        val confirmExitMessageDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_message_dialog, null)
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
            activity.initFinishProcess(false)
        }

        noButton.setOnClickListener {
            buttonPressed()
        }

        confirmExitMessageDialog.show()
    }

}