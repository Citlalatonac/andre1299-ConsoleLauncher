package ohi.andre.consolelauncher.ui.listview;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.managers.SkinManager;
import ohi.andre.consolelauncher.managers.SuggestionsManager;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.DirectorySeeker;
import ohi.andre.consolelauncher.tuils.interfaces.StatusBarHider;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionInterface;
import ohi.andre.consolelauncher.ui.UIManager;
import ohi.andre.realtimetyping.TextViewManager;

/**
 * Created by andre on 03/11/15.
 */
public class ListViewUIManager extends UIManager implements View.OnLongClickListener {

    private LinearLayout commandsView;
    private Button submit;

    private String separatorText;

    private SkinManager skinManager;
    private LayoutInflater inflater;
    private boolean showSubmit;
    private boolean showSuggestions;

    private String hint;

    public ListViewUIManager(Context context, View rootView, CommandExecuter tri, Handler h, DevicePolicyManager mgr, ComponentName name,
                             PreferencesManager prefsMgr, DirectorySeeker seeker, boolean useSystemWP, StatusBarHider sbh,
                             SuggestionsManager suggestionsManager, SuggestionInterface suggestionInterface) {
        super(context, rootView, tri, h, mgr, name, prefsMgr, seeker, useSystemWP, sbh, suggestionsManager, suggestionInterface);
    }

    @Override
    protected void setupViews(SkinManager skinManager, PreferencesManager preferencesManager, Context c, Handler h, View rootView,
                              boolean inputBottom, boolean showSuggestions) {
        commandsView = (LinearLayout) rootView.findViewById(R.id.commandview_ll);
        commandsView.setGravity(inputBottom ? Gravity.BOTTOM : Gravity.TOP);
        commandsView.setOnTouchListener(this);

        rootView.findViewById(R.id.scroll_sv).setOnTouchListener(this);

        this.skinManager = skinManager;

        this.hint = getHint(preferencesManager);

        this.showSubmit = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.SHOWSUBMIT));
        this.showSuggestions = showSuggestions;
    }

    @Override
    protected void onSeparatorChanged(String s) {
        separatorText = s;
    }

    @Override
    protected TextViewManager getTvMgr() {
        return null;
    }

    @Override
    public void clear() {
        commandsView.removeAllViews();

        input = null;
        output = null;
    }

    @Override
    public void onResult() {
//        input & output are changed by the superclass
//        need to add a new CommandView

        if(input != null) {
            input.setFocusable(false);
            input.setClickable(false);
        }

        if(submit != null) {
            submit.setOnClickListener(null);
            submit.setVisibility(View.GONE);
        }

        if(inflater == null)
            inflater = ((Activity) super.mContext).getLayoutInflater();
        View commandView = inflater.inflate(R.layout.input_output_listview, null);

        commandsView.addView(commandView, commandsView.getChildCount());

        input = (EditText) commandView.findViewById(R.id.input_et);
        output = (TextView) commandView.findViewById(R.id.output_tv);
        TextView separator = (TextView) commandView.findViewById(R.id.separator_tv);

        submit = (Button) commandView.findViewById(R.id.submit_tv);
        if(showSubmit) {
            submit.setOnClickListener(this);
            skinManager.setupSubmit(submit);
        } else {
            submit.setVisibility(View.GONE);
            submit = null;
        }

//        positive id for input
        input.setId(commandsView.getChildCount());
//        negative id for output
        output.setId(commandsView.getChildCount() * -1);
//        0 = separator
        separator.setId(0);

        input.setOnTouchListener(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setOnEditorActionListener(this);
        input.setOnLongClickListener(this);

//        set only if textwatcher isnt null
        if(showSuggestions)
            input.addTextChangedListener(textWatcher);

//        this throws an exception in onCreate
        try {
            input.setText("");
        } catch (NullPointerException e) {}

        output.setOnTouchListener(this);

        separator.setText(separatorText);

        skinManager.setupInput(input);
        skinManager.setupOutput(output);
        skinManager.setupInput(separator);

        input.setHint(hint);
        input.requestFocus();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(verifyDoubleTap(event))
            return true;

        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return false;

        int id = v.getId();

//        input
        if(id > 0 && input != null && v instanceof TextView) {
            if(id == commandsView.getChildCount())
                processOpenInputRequest();

        }
        v.onTouchEvent(event);

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() > 0 && v.getId() != commandsView.getChildCount() && input != null && v instanceof TextView) {
            input.setText(((TextView) v).getText());
            openKeyboard();
        }
        return false;
    }
}