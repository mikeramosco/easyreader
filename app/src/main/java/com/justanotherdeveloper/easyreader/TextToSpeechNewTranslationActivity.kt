package com.justanotherdeveloper.easyreader

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.justanotherdeveloper.easyreader.databinding.ActivityTextToSpeechNewTranslationBinding

class TextToSpeechNewTranslationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextToSpeechNewTranslationBinding
    private lateinit var local: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initListeners()

        local = TinyDB(this)
    }

    private fun initBinding() {
        binding = ActivityTextToSpeechNewTranslationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initListeners() {
        binding.saveButton.setOnClickListener { saveButtonPressed() }
        binding.backArrow.setOnClickListener { finish() }
    }

    private fun saveButtonPressed() {
        val original = binding.originalField.text.toString()
        val translation = binding.translationField.text.toString()

        if(original.isNotEmpty()) {
            addTranslation(original, translation, local)
            finish()
        } else showEmptyFieldMessage()
    }

    private fun showEmptyFieldMessage() {
        showToast(getString(R.string.emptyFieldError))
    }
}