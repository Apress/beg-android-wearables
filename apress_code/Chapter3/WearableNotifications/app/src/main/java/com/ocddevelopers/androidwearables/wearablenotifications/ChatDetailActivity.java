package com.ocddevelopers.androidwearables.wearablenotifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

/**
 * Represents a chat conversation with a specific person. Contains placeholder content.
 */
public class ChatDetailActivity extends ActionBarActivity {
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    public static final String EXTRA_CHATTING_WITH = "chatting_with";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        CharSequence replyText = getMessageText(getIntent());
        if(replyText != null) {
            TextView replyTextView = (TextView)findViewById(R.id.reply);
            replyTextView.setText("You replied: " + replyText);
        }

        String chattingWith = getIntent().getStringExtra(EXTRA_CHATTING_WITH);
        if(chattingWith != null) {
            getSupportActionBar().setTitle(chattingWith);
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }
}
