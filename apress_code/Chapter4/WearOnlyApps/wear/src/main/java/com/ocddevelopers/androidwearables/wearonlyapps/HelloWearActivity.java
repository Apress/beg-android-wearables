package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.Activity;
import android.os.Bundle;

/**
 * Shows how to create a minimal app for Andrdoid Wear.
 */
public class HelloWearActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
    }
}
