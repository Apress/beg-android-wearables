package com.ocddevelopers.androidwearables.glasswarebasics;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how to build a list of scrolling cards.
 */
public class ScrollingCardsActivity extends Activity {
    private CardScrollView mCardScrollView;
    private List<String> mWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWords = new ArrayList<String>();
        mWords.add("one");
        mWords.add("two");
        mWords.add("three");
        mWords.add("four");
        mWords.add("five");
        mWords.add("six");
        mWords.add("seven");

        TextCardAdapter textCardAdapter = new TextCardAdapter(this, mWords);
        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(textCardAdapter);
        mCardScrollView.setOnItemClickListener(itemClickListener);

        setContentView(mCardScrollView);
    }

    private AdapterView.OnItemClickListener itemClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(Sounds.TAP);

            Toast.makeText(ScrollingCardsActivity.this, "Clicked on item " + mWords.get(position),
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mCardScrollView.activate();
    }

    @Override
    protected void onPause() {
        mCardScrollView.deactivate();
        super.onPause();
    }

    private static class TextCardAdapter extends CardScrollAdapter {
        private Context mContext;
        private List<String> mItems;

        public TextCardAdapter(Context context, List<String> items) {
            mContext = context;
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            TextView textView;

            if(view == null) {
                textView = new TextView(mContext);
            } else {
                textView = (TextView)view;
            }

            textView.setText(mItems.get(position));

            return textView;
        }

        @Override
        public int getPosition(Object item) {
            return mItems.indexOf(item);
        }
    }
}
