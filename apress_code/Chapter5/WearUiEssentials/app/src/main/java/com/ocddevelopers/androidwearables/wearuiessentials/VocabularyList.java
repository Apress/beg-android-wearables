package com.ocddevelopers.androidwearables.wearuiessentials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a list of VocabularyWords and contains the static fromJson method, which parses
 * a list of words from a JSON string.
 */
public class VocabularyList {
    private List<VocabularyWord> mWords;

    public static VocabularyList fromJson(String jsonVocabularyList) {
        List<VocabularyWord> words = new ArrayList<VocabularyWord>();

        try {
            JSONObject wordListObject = new JSONObject(jsonVocabularyList);
            JSONArray wordListArray = wordListObject.getJSONArray("words");

            for(int i=0; i<wordListArray.length(); ++i) {
                JSONObject wordObject = wordListArray.getJSONObject(i);
                String word = wordObject.getString("word");
                String category = wordObject.getString("category");
                String definition = wordObject.getString("definition");
                String exampleSentence = wordObject.getString("example_sentence");
                String synonyms = wordObject.optString("synonyms");

                VocabularyWord vocabularyWord = new VocabularyWord(word, category,
                        definition, exampleSentence);
                if(!synonyms.isEmpty()) {
                    vocabularyWord.setSynonyms(synonyms);
                }
                words.add(vocabularyWord);
            }

            return new VocabularyList(words);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private VocabularyList(List<VocabularyWord> vocabularyWordList) {
        mWords = vocabularyWordList;
    }

    public int size() {
        return mWords.size();
    }

    public VocabularyWord getVocabularyWord(int position) {
        return mWords.get(position);
    }
}
