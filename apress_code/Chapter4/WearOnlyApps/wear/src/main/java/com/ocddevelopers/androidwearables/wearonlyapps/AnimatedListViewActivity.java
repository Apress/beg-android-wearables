package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.Toast;

/**
 * Shows how to create a WearableListView similar to the one used to select the duration
 * of a timer in Android Wear. Every item in the list contains a blue circle on the left.
 * When a list item enters the central position, its circle animates to a larger size. When
 * a list item leaves the central position, its circle animates back to the original size.
 */
public class AnimatedListViewActivity extends Activity {
    private String[] mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlistview);

        mItems = new String[] { "item0", "item1", "item2", "item3", "item4" };
        WearableListView wearableListView = (WearableListView) findViewById(R.id.list);
        wearableListView.setAdapter(new WearableAdapter(this, mItems));
        wearableListView.setClickListener(mClickListener);
    }

    private WearableListView.ClickListener mClickListener = new WearableListView.ClickListener() {
        @Override
        public void onClick(WearableListView.ViewHolder viewHolder) {
            int position = viewHolder.getPosition();
            Toast.makeText(AnimatedListViewActivity.this, "Tapped on " + position,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTopEmptyRegionClick() {

        }
    };
}
