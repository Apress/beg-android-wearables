package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Demonstrates the use of GridViewPager and CardFragment.
 *
 * This wearable app helps improve your vocabulary by giving you a list of vocabulary words and
 * their respective definitions, example sentences, and synonyms.
 */
public class VocabularyActivity extends Activity {
    private GridViewPager mGridViewPager;
    private String jsonVocabularyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        VocabularyList vocabularyList = null;

        try {
            // a sample vocabulary list is included in the project as an asset
            InputStream is = getAssets().open("sample_vocabulary_list");

            // read the entire vocabulary list. The delimiter "\A" matches the beginning of input;
            // as a result, the scanner will read the entire stream.
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            jsonVocabularyList = scanner.hasNext() ? scanner.next() : "";
            is.close();
            vocabularyList = VocabularyList.fromJson(jsonVocabularyList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(vocabularyList == null) {
            throw new RuntimeException("Invalid vocabulary list.");
        }

        mGridViewPager = (GridViewPager)findViewById(R.id.gridview);
        mGridViewPager.setAdapter(new VocabularyAdapter(this, getFragmentManager(), vocabularyList));
    }

}
