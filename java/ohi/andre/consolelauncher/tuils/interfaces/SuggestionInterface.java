package ohi.andre.consolelauncher.tuils.interfaces;

import android.view.ViewGroup;

import ohi.andre.consolelauncher.managers.SuggestionsManager;

/**
 * Created by francescoandreuzzi on 27/12/15.
 */
public interface SuggestionInterface {

    void requestSuggestion(String before, String lastWord, SuggestionsManager suggestionsManager, ViewGroup rootView,
                           SuggestionGetter getter);
}
