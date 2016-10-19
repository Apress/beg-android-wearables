package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;

/**
 * Displays a vocabulary word per row. Every row contains three columns with the word's
 * definition, an example sentence, and an optional list of synonyms.
 */
public class VocabularyAdapter extends FragmentGridPagerAdapter {
    private VocabularyList mVocabularyList;
    private Drawable mBackground;

    public VocabularyAdapter(Context context, FragmentManager fm, VocabularyList vocabularyList) {
        super(fm);
        mVocabularyList = vocabularyList;
        mBackground = context.getDrawable(R.drawable.example_bg);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        VocabularyWord word = mVocabularyList.getVocabularyWord(row);
        String description = null;
        switch (col) {
            case 0:
                description = word.getDefinition();
                break;
            case 1:
                description = "\"" + word.getExampleSentence() + "\"";
                break;
            case 2:
                description = word.getSynonyms();
                break;
        }

        CardFragment cardFragment = CardFragment.create(word.getWord() +
                " (" + word.getCategory() + ")", description);
        cardFragment.setCardGravity(Gravity.BOTTOM);
        cardFragment.setExpansionEnabled(true);
        cardFragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
        cardFragment.setExpansionFactor(2f);

        return cardFragment;
    }

    @Override
    public Drawable getBackgroundForRow(int row) {
        return mBackground;
    }

    @Override
    public int getRowCount() {
        return mVocabularyList.size();
    }

    @Override
    public int getColumnCount(int row) {
        if(mVocabularyList.getVocabularyWord(row).hasSynonyms()) {
            return 3;
        } else {
            return 2;
        }
    }

    // Uncomment to test out fixed-movement paging.
    /*
    @Override
    public int getCurrentColumnForRow(int row, int currentColumn) {
        return currentColumn;
    }
    */
}