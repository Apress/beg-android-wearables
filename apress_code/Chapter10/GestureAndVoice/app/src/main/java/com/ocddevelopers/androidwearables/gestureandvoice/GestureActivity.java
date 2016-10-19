package com.ocddevelopers.androidwearables.gestureandvoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;

/**
 * Displays the gestures captured by GestureDetector for demonstration purposes.
 */
public class GestureActivity extends Activity {
    private GestureDetector mGestureDetector;
    private CardBuilder mCardBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);
        mGestureDetector.setFingerListener(mFingerListener);

        mCardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT);
        updateText("Listening for gestures...");
    }

    private void updateText(String text) {
        mCardBuilder.setText(text);

        // although we just updated mCardBuilder, we won't see any change unless
        // we call setContentView.
        setContentView(mCardBuilder.getView());
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event) || mGestureDetector.onMotionEvent(event);
    }

    private GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            switch(gesture) {
                case TAP:
                    updateText("TAP detected");
                    return true;
                case TWO_TAP:
                    updateText("TWO_TAP detected");
                    return true;
                case THREE_TAP:
                    updateText("THREE_TAP detected");
                    return true;
                case LONG_PRESS:
                    updateText("LONG_PRESS detected");
                    return true;
                case TWO_LONG_PRESS:
                    updateText("TWO_LONG_PRESS detected");
                    return true;
                case THREE_LONG_PRESS:
                    updateText("THREE_LONG_PRESS detected");
                    return true;
                case SWIPE_UP:
                    updateText("SWIPE_UP detected");
                    return true;
                case TWO_SWIPE_UP:
                    updateText("TWO_SWIPE_UP detected");
                    return true;
                case SWIPE_RIGHT:
                    updateText("SWIPE_RIGHT detected");
                    return true;
                case TWO_SWIPE_RIGHT:
                    updateText("TWO_SWIPE_RIGHT detected");
                    return true;
                case SWIPE_DOWN:
                    updateText("SWIPE_DOWN detected");
                    // returning false allows the activity to process the gesture
                    // swiping down wouldn't dismiss the activity if we returned true
                    return false;
                case TWO_SWIPE_DOWN:
                    // this will never be called
                    updateText("TWO_SWIPE_DOWN detected");
                    return true;
                case SWIPE_LEFT:
                    updateText("SWIPE_LEFT detected");
                    return true;
                case TWO_SWIPE_LEFT:
                    updateText("TWO_SWIPE_LEFT detected");
                    return true;
                default:
                    return false;
            }
        }
    };

    private GestureDetector.FingerListener mFingerListener = new GestureDetector.FingerListener() {
        @Override
        public void onFingerCountChanged(int previousCount, int currentCount) {
            mCardBuilder.setFootnote(currentCount + " fingers on touchpad");
            setContentView(mCardBuilder.getView());
        }
    };

}
