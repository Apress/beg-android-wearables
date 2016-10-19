package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows how to create a basic WearableListView.
 */
public class SimpleWearableListViewActivity extends Activity {
    private String[] mItems;
    private WearableListView mWearableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlistview);

        mItems = new String[] { "item0", "item1", "item2", "item3", "item4" };
        mWearableListView = (WearableListView) findViewById(R.id.list);
        mWearableListView.setAdapter(new SampleAdapter(this, mItems));
        mWearableListView.setClickListener(mClickListener);
    }

    private WearableListView.ClickListener mClickListener = new WearableListView.ClickListener() {
        @Override
        public void onClick(WearableListView.ViewHolder viewHolder) {
            int position = viewHolder.getPosition();
            Toast.makeText(SimpleWearableListViewActivity.this, "Tapped on item " + position,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onTopEmptyRegionClick() {
            Toast.makeText(SimpleWearableListViewActivity.this, "Tapped on top empty region",
                    Toast.LENGTH_LONG).show();
        }
    };

    public static class SampleAdapter extends WearableListView.Adapter {
        private LayoutInflater mInflater;
        private String[] mSampleItems;

        public SampleAdapter(Context context, String[] sampleItems) {
            mInflater = LayoutInflater.from(context);
            mSampleItems = sampleItems;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = mInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            return new WearableListView.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
            TextView textView = (TextView) viewHolder.itemView;
            textView.setText(mSampleItems[position]);
        }

        @Override
        public int getItemCount() {
            return mSampleItems.length;
        }
    }

}
