package com.ocddevelopers.androidwearables.glasswarebasics;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

/**
 * Demonstrates how to provide tuggable feedback to indicate that swiping has no effect.
 * Additionally, plays the DISALLOWED sound when a user taps on the touchpad to indicate that
 * this gesture also has no effect.
 */
public class BetterHelloWorldActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView helloWorld = new TextView(this);
        helloWorld.setText("Hello World");

        TuggableView tuggableView = new TuggableView(this, helloWorld);
        tuggableView.setOnItemClickListener(mItemClickListener);

        setContentView(tuggableView);
    }

    private AdapterView.OnItemClickListener mItemClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(Sounds.DISALLOWED);
        }
    };

}