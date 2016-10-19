package com.ocddevelopers.androidwearables.gestureandvoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Displays generic motion events for demonstration purposes.
 */
public class MotionEventActivity extends Activity {
    private TextView mFingerCount, mAction, mPositionX, mPositionY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motionevent);

        mFingerCount = (TextView) findViewById(R.id.fingercount);
        mAction = (TextView) findViewById(R.id.action);
        mPositionX = (TextView) findViewById(R.id.positionx);
        mPositionY = (TextView) findViewById(R.id.positiony);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        String action = "";

        int fingerCount = event.getPointerCount();
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";

                if(fingerCount == 1) {
                    fingerCount = 0;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;
            default:
                return false;
        }

        mAction.setText(action);
        mFingerCount.setText(fingerCount + " fingers on touchpad");
        mPositionX.setText("" + event.getX());
        mPositionY.setText("" + event.getY());

        return super.onGenericMotionEvent(event);
    }

}
