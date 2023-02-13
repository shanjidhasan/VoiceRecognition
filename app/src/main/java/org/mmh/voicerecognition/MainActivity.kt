package org.mmh.voicerecognition

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import org.mmh.voicerecognition.R
import java.util.*

class MainActivity : AppCompatActivity(), RecognitionListener {

    // on below line we are creating variables
    // for text view and image view
    lateinit var outputTV: TextView
    lateinit var micIV: ImageView
    private val activationKeyword: String = "Hello"
    private val shouldMute: Boolean = false
    private val callback: RecognitionCallback? = null
    private var isActivated: Boolean = false
    private val speech: SpeechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(applicationContext) }
    private val audioManager: AudioManager? = applicationContext.getSystemService() as AudioManager?

    // on below line we are creating a constant value
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initializing variables of list view with their ids.
        outputTV = findViewById(R.id.idTVOutput)
        micIV = findViewById(R.id.idIVMic)

        // on below line we are adding on click
        // listener for mic image view.
        micIV.setOnClickListener {
            // on below line we are calling speech recognizer intent.

        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, applicationContext.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
//            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//
//            // on below line we are passing language model
//            // and model free form in our intent
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//
//            // on below line we are passing our
//            // language as a default language.
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE,
//                Locale.getDefault()
//            )

            // on below line we are specifying a prompt
            // message as speak to text on below line.
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@MainActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

    override fun onBeginningOfSpeech() {
        callback?.onBeginningOfSpeech()
    }

    override fun onReadyForSpeech(params: Bundle) {
        muteRecognition(shouldMute || !isActivated)
        callback?.onReadyForSpeech(params)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        callback?.onBufferReceived(buffer)
    }

    override fun onRmsChanged(rmsdB: Float) {
        callback?.onRmsChanged(rmsdB)
    }

    override fun onEndOfSpeech() {
        callback?.onEndOfSpeech()
    }

    override fun onError(errorCode: Int) {
        if (isActivated) {
            callback?.onError(errorCode)
        }
        isActivated = false

        when (errorCode) {
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> cancelRecognition()
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                destroyRecognizer()
                createRecognizer()
            }
        }

        startRecognition()
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        callback?.onEvent(eventType, params)
    }

    override fun onPartialResults(partialResults: Bundle) {
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (isActivated && matches != null) {
            callback?.onPartialResults(matches)
        }
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (matches != null) {
            if (isActivated) {
                isActivated = false
                callback?.onResults(matches, scores)
                stopRecognition()
            } else {
                matches.firstOrNull { it.contains(other = activationKeyword, ignoreCase = true) }
                    ?.let {
                        isActivated = true
                        callback?.onKeywordDetected()
                    }
                startRecognition()
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun muteRecognition(mute: Boolean) {
        audioManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
                it.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_RING, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0)
            } else {
                it.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute)
                it.setStreamMute(AudioManager.STREAM_ALARM, mute)
                it.setStreamMute(AudioManager.STREAM_MUSIC, mute)
                it.setStreamMute(AudioManager.STREAM_RING, mute)
                it.setStreamMute(AudioManager.STREAM_SYSTEM, mute)
            }
        }
    }
}

//    // on below line we are calling on activity result method.
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // in this method we are checking request
//        // code with our result code.
//        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
//            // on below line we are checking if result code is ok
//            if (resultCode == RESULT_OK && data != null) {
//
//                // in that case we are extracting the
//                // data from our array list
//                val res: ArrayList<String> =
//                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
//
//                // on below line we are setting data
//                // to our output text view.
//                outputTV.setText(
//                    Objects.requireNonNull(res)[0]
//                )
//            }
//        }
//    }
//}
