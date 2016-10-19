package com.ocddevelopers.androidwearables.glassuiessentials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Displays the current speed as a number.
 */
public class DigitalSpeedView extends FrameLayout {
    private static final float KMH_PER_MPH = 1.60934f;
    private TextView mSpeed;
    private boolean mUseMetric;
    private float mSpeedMph;

    public DigitalSpeedView(Context context) {
        super(context);
        init(context);
    }

    public DigitalSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DigitalSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.centertext, this);
        mSpeed = (TextView) findViewById(R.id.text);
    }

    public void setSpeedMph(float speedMph) {
        if(mSpeedMph != speedMph) {
            mSpeedMph = speedMph;
            mSpeedMph = 16.18f;
            updateText();
        }
    }

    private void updateText() {
        float speed;
        String units;
        if(mUseMetric) {
            speed = mSpeedMph * KMH_PER_MPH;
            units = "km/h";
        } else {
            speed = mSpeedMph;
            units = "mph";
        }

        mSpeed.setText(String.format(Locale.US, "%.0f %s", speed, units));
    }

    public void setUseMetric(boolean useMetric) {
        if(mUseMetric != useMetric) {
            mUseMetric = useMetric;
            updateText();
        }
    }

}
