package com.justanotherdeveloper.easyreader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.justanotherdeveloper.easyreader.databinding.ActivityTextToSpeechTranslatorBinding
import java.util.Locale

class TextToSpeechTranslatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextToSpeechTranslatorBinding
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var local: TinyDB

    private val textToSpeechMap = HashMap<String, String>()

    private var textToSpeechIsFunctional = false
    private var previewPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.matchCaseSwitch.isChecked = matchCase()
        initListeners()

        local = TinyDB(this)
        updateTranslationsList()

        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                previewPlaying = false
            }
            override fun onError(utteranceId: String?) {}
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeechIsFunctional = true
                textToSpeech.language = Locale.US
                textToSpeechMap[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceID
                textToSpeech.setOnUtteranceProgressListener(speechListener)
                textToSpeech.setSpeechRate(1f)
            }
        }
    }

    private fun initBinding() {
        binding = ActivityTextToSpeechTranslatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initListeners() {
        binding.addButton.setOnClickListener { addButtonPressed() }
        binding.backArrow.setOnClickListener { finish() }
        binding.matchCaseSwitch.setOnCheckedChangeListener { _, isChecked ->
            setMatchCase(isChecked, local)
        }
    }

    private fun addButtonPressed() {
        val intent = Intent(this, TextToSpeechNewTranslationActivity::class.java)
        startActivityForResult(intent, 0)
    }

    private fun updateTranslationsList() {
        binding.translationsContainer.removeAllViews()
        val translations = getTranslations()
        for(orig in translations.keys.sorted()) {
            val translation = translations[orig]!!
            addTranslationView(orig, translation)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addTranslationView(orig: String, translation: String) {
        val translationView = layoutInflater.inflate(R.layout.widget_translation, null)
        val originalText = translationView.findViewById<TextView>(R.id.originalText)
        val translationText = translationView.findViewById<TextView>(R.id.translationText)
        originalText.text = "\"$orig\""
        translationText.text = "\"$translation\""

        val removeButton = translationView.findViewById<ImageView>(R.id.removeButton)
        removeButton.setOnClickListener { removeTranslationPressed(translationView, orig) }

        val previewButton = translationView.findViewById<ImageView>(R.id.previewButton)
        previewButton.setOnClickListener { previewButtonPressed(translation) }

        binding.translationsContainer.addView(translationView)
    }

    private fun previewButtonPressed(translation: String) {
        if(previewPlaying || !textToSpeechIsFunctional) return
        previewPlaying = true
        textToSpeech.speak(translation,
            TextToSpeech.QUEUE_FLUSH, null, utteranceID)
    }

    private fun removeTranslationPressed(translationView: View, orig: String) {
        beginTransition(binding.translationsContainer)
        binding.translationsContainer.removeView(translationView)
        removeTranslation(orig, local)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateTranslationsList()
    }
}