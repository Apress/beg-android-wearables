package com.ocddevelopers.androidwearables.glasswarebasics;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Shows how to create a basic Hello World app with the GDK.
 */
public class HelloWorldActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView helloWorld = new TextView(this);
        helloWorld.setText("Hello World");
        setContentView(helloWorld);
    }

}