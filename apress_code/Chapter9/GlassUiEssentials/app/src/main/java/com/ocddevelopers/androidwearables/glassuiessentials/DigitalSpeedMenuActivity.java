package com.ocddevelopers.androidwearables.glassuiessentials;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Creates a menu for the LiveCard started from DigitalSpeedService.
 */
public class DigitalSpeedMenuActivity extends Activity {
    public static final String EXTRA_USE_METRIC = "use_metric";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean useMetric = getIntent().getBooleanExtra(EXTRA_USE_METRIC, false);
        if(useMetric) {
            menu.findItem(R.id.change_units).setTitle("Use mph");
        } else {
            menu.findItem(R.id.change_units).setTitle("Use Km/h");
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.digital_speed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.stop:
                stopService(new Intent(this, DigitalSpeedService.class));
                return true;
            case R.id.change_units:
                Intent resetIntent = new Intent(this, DigitalSpeedService.class);
                resetIntent.setAction(DigitalSpeedService.ACTION_CHANGE_UNITS);
                startService(resetIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        finish();
    }

}
