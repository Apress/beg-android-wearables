package com.ocddevelopers.androidwearables.wearonlyapps;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Used by AnimatedListViewActivity to populate a WearableListView.
 */
public class WearableAdapter extends WearableListView.Adapter {
    private List<String> mItems;
    private final LayoutInflater mInflater;

    public WearableAdapter(Context context, String[] items) {
        this(context, Arrays.asList(items));
    }

    public WearableAdapter(Context context, List<String> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        TextView textView = itemViewHolder.mItemTextView;
        textView.setText(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView mItemTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mItemTextView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
