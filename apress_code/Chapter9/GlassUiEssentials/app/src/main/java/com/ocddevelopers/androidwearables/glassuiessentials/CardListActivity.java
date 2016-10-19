package com.ocddevelopers.androidwearables.glassuiessentials;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how to use a variety of CardBuilder layouts.
 */
public class CardListActivity extends Activity {
    private CardScrollView mCardScrollView;
    private List<CardBuilder> mCards;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        initCards();
        mCardScrollView = new CardScrollView(this);
        CardListScrollAdapter adapter = new CardListScrollAdapter(mCards);
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.setOnItemClickListener(mItemClickListener);

        setContentView(mCardScrollView);
    }

    private AdapterView.OnItemClickListener mItemClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // last item clicked
            if (position == mCards.size() - 1) {
                GlassAlertDialog alertDialog = new GlassAlertDialog(CardListActivity.this,
                        R.drawable.ic_timer_50, "alert title here", "footnote");

                alertDialog.setOnClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // no need to play Sounds.TAP. The dialog does it automatically.
                        Toast.makeText(CardListActivity.this, "Alert Dialog clicked",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.show();
            } else {
                mAudioManager.playSoundEffect(Sounds.DISALLOWED);
            }
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


    private void initCards() {
        mCards = new ArrayList<CardBuilder>();

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("This card uses the TEXT layout.")
                .setFootnote("Footnote")
                .setTimestamp("just now"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("This card uses the TEXT layout and has a background image.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.falcon));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("This card uses the TEXT layout and has a mosaic of four images as a background.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.fox)
                .addImage(R.drawable.hare)
                .addImage(R.drawable.falcon)
                .addImage(R.drawable.toad));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("This card uses the TEXT layout and has a mosaic of eight images as a background.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.fox)
                .addImage(R.drawable.hare)
                .addImage(R.drawable.falcon)
                .addImage(R.drawable.toad)
                .addImage(R.drawable.wolf)
                .addImage(R.drawable.chameleon)
                .addImage(R.drawable.pig)
                .addImage(R.drawable.monkey));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED)
                .setText("This card uses the TEXT FIXED layout. The text size will automatically adjust to best fit the available space.")
                .setFootnote("Footnote")
                .setTimestamp("just now"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.COLUMNS)
                .setText("This card uses the COLUMNS layout. It displays an image on the left side.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.falcon));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.COLUMNS)
                .setText("This card uses the COLUMNS layout. It displays a mosaic of three image on the left side.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.falcon)
                .addImage(R.drawable.fox)
                .addImage(R.drawable.hare));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.COLUMNS)
                .setText("This card uses the COLUMNS layout and contains a centered icon on the left side.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .setIcon(R.drawable.ic_timer_50));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.COLUMNS_FIXED)
                .setText("This card uses the COLUMNS_FIXED layout. It's just like COLUMNS but with text that automatically resizes to fit the available space.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.toad)
                .addImage(R.drawable.fox)
                .addImage(R.drawable.falcon)
                .addImage(R.drawable.hare));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.CAPTION)
                .setText("The CAPTION layout.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .addImage(R.drawable.falcon));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TITLE)
                .setText("The TITLE layout")
                .setIcon(R.drawable.ic_timer_50)
                .addImage(R.drawable.falcon));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.AUTHOR)
                .setText("The AUTHOR layout displays an author's avatar, name, and subheading along with a message. A twitter message could use this layout.")
                .setIcon(R.drawable.fox)
                .setHeading("Fox Vulpes")
                .setSubheading("San Diego, California")
                .setFootnote("Footnote")
                .setTimestamp("just now"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU)
                .setText("The MENU layout")
                .setIcon(R.drawable.ic_timer_50)
                .setFootnote("menu item description here"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("Tap on this card to see a dialog with the ALERT layout.")
                .setFootnote("Footnote")
                .setTimestamp("just now")
                .showStackIndicator(true));
    }

    public static class GlassAlertDialog extends Dialog {
        private OnClickListener mClickListener;
        private AudioManager mAudioManager;
        private GestureDetector mGestureDetector;

        public GlassAlertDialog(Context context, int iconRes, String text, String footnote) {
            super(context, R.style.DialogTheme);

            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mGestureDetector = new GestureDetector(context);
            mGestureDetector.setBaseListener(mBaseListener);

            setContentView(new CardBuilder(context, CardBuilder.Layout.ALERT)
                    .setIcon(iconRes)
                    .setText(text)
                    .setFootnote(footnote)
                    .getView());
        }

        @Override
        public boolean onGenericMotionEvent(MotionEvent event) {
            return super.onGenericMotionEvent(event) || mGestureDetector.onMotionEvent(event);
        }

        private GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if(gesture == Gesture.TAP) {
                    mAudioManager.playSoundEffect(Sounds.TAP);

                    if(mClickListener != null) {
                        // Glass dialogs don't have buttons. thus, which is always 0
                        mClickListener.onClick(GlassAlertDialog.this, 0);
                    }

                    dismiss();
                    return true;
                }

                return false;
            }
        };

        public OnClickListener getOnClickListener() {
            return mClickListener;
        }

        public void setOnClickListener(OnClickListener onClickListener) {
            mClickListener = onClickListener;
        }
    }

}
