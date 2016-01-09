package ohi.andre.consolelauncher.tuils.interfaces;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by francescoandreuzzi on 27/12/15.
 */
public interface SuggestionGetter {

    TextView getSuggestionView(String text, Context context);
}
