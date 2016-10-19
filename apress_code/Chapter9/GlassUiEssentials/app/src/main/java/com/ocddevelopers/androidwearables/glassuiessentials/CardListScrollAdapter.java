package com.ocddevelopers.androidwearables.glassuiessentials;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

/**
 * An adapter used to display a list of CardBuilder views on a CardScrollAdapter.
 */
public class CardListScrollAdapter extends CardScrollAdapter {
    private List<CardBuilder> mCards;

    public CardListScrollAdapter(List<CardBuilder> cards) {
        mCards = cards;
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mCards.get(position).getItemViewType();
    }

    @Override
    public int getPosition(Object item) {
        return mCards.indexOf(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mCards.get(position).getView(convertView, parent);
    }

}
