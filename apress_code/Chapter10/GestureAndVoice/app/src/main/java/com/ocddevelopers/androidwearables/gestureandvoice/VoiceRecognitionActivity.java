package com.ocddevelopers.androidwearables.gestureandvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how to use 1) voice prompts, 2) contextual voice commands, and 3) speech recognizer.
 */
public class VoiceRecognitionActivity extends Activity {
    private static final int SPEECH_REQUEST = 1;
    private CardBuilder mCardBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for contextual voice commands
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        mCardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT);

        ArrayList<String> voiceResults = getIntent().getExtras()
                .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        String text = voiceResults.get(0);
        updateText(text);
    }

    private void updateText(String text) {
        mCardBuilder.setText(text);
        setContentView(mCardBuilder.getView());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.voicerec:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
        startActivityForResult(intent, SPEECH_REQUEST);
    }


    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.voicerec, menu);
            return true;
        }

        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id.voicerec:
                    displaySpeechRecognizer();
                    return true;
                default:
                    return true;
            }
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            updateText(spokenText);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
