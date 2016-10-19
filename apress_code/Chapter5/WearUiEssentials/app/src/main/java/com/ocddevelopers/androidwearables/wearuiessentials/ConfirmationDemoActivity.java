package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

/**
 * Demonstrates the usage of CircledImageView, DelayedConfirmationView, WatchViewStub, and
 * ConfirmationActivity.
 */
public class ConfirmationDemoActivity extends Activity {
    private Fragment[] mFragments;
    private GridViewPager mGridViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_demo);

        // The first three fragments demonstrate ConfirmationActivity's success, failure, and
        // open on phone animations, respectively. The fourth fragment demonstrates the use of
        // DelayedConfirmationView.
        mFragments = new Fragment[] {
                ActionFragment.newInstance("Success", R.drawable.ic_full_check,
                        makeConfirmationActivityIntent(ConfirmationActivity.SUCCESS_ANIMATION,
                                "success animation")),
                ActionFragment.newInstance("Failure", R.drawable.ic_full_cancel,
                        makeConfirmationActivityIntent(ConfirmationActivity.FAILURE_ANIMATION,
                                "failure animation")),
                ActionFragment.newInstance("Open on Phone", R.drawable.ic_full_reply,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "open on phone animation")),
                ActionFragment.newInstance("Delayed Confirmation", R.drawable.ic_full_check,
                        new Intent(this, DelayedConfirmationActivity.class))
        };

        mGridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        mGridViewPager.setAdapter(new ConfirmationDemoGridPagerAdapter(this,
                getFragmentManager(), mFragments));
    }

    private Intent makeConfirmationActivityIntent(int animationType, String message) {
        Intent confirmationActivity = new Intent(this, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        return confirmationActivity;
    }

    public static class ConfirmationDemoGridPagerAdapter extends FragmentGridPagerAdapter {
        private Drawable mBackground;
        private Fragment[] mDemoFragments;

        public ConfirmationDemoGridPagerAdapter(Context context, FragmentManager fm, Fragment[] fragments) {
            super(fm);
            mBackground = context.getDrawable(R.drawable.definition_bg);
            mDemoFragments = fragments;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return mDemoFragments[column];
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mDemoFragments.length;
        }

        @Override
        public Drawable getBackgroundForPage(int row, int column) {
            return mBackground;
        }
    }

}
