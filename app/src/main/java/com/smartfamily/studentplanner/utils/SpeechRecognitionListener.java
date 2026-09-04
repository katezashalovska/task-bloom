package com.smartfamily.studentplanner.utils;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfamily.studentplanner.R;

import java.util.ArrayList;

public class SpeechRecognitionListener implements RecognitionListener {

    private AppCompatActivity activity;  // Reference to the Activity that invokes this listener
    private EditText titleInput;  // Reference to the EditText field for the task title
    private EditText descriptionInput;  // Reference to the EditText field for the task description

    // Constructor to pass activity and input fields into the listener
    public SpeechRecognitionListener(AppCompatActivity activity, EditText titleInput, EditText descriptionInput) {
        this.activity = activity;
        this.titleInput = titleInput;
        this.descriptionInput = descriptionInput;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {
        // Show a Toast message with the error code
        Toast.makeText(activity, R.string.speech_recognition_error + error, Toast.LENGTH_SHORT).show();
    }

    // Handles the speech recognition results
    @Override
    public void onResults(Bundle results) {
        // Get the list of possible recognized phrases
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String spokenText = matches.get(0);  // Use the first recognized phrase

            // Check which EditText (Title or Description) is focused and update it with the spoken text
            if (titleInput.hasFocus()) {
                titleInput.setText(spokenText);
            } else if (descriptionInput.hasFocus()) {
                descriptionInput.setText(spokenText);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {}

    @Override
    public void onEvent(int eventType, Bundle params) {}
}