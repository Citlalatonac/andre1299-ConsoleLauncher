package ohi.andre.consolelauncher.ui.fileview;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.managers.SkinManager;
import ohi.andre.consolelauncher.managers.SuggestionsManager;
import ohi.andre.consolelauncher.tuils.FileAdapter;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.DirectorySeeker;
import ohi.andre.consolelauncher.tuils.interfaces.FileOpener;
import ohi.andre.consolelauncher.tuils.interfaces.StatusBarHider;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionInterface;
import ohi.andre.consolelauncher.ui.UIManager;
import ohi.andre.realtimetyping.TextViewManager;

/**
 * Created by andre on 03/11/15.
 */
public class FileViewUIManager extends UIManager implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final int FILECOLUMNS_DEFAULT = 2;
    private final int OUTPUT_MILLISECONDS = 5;

    private TextViewManager tvMgr;
    private TextView separator, dir;
    private FileAdapter fileAdapter;

    private FileOpener opener;
    private boolean showHiddenFiles;

    public FileViewUIManager(Context context, View rootView, CommandExecuter tri, FileOpener op, Handler h, DevicePolicyManager mgr,
                             ComponentName name, DirectorySeeker seeker, PreferencesManager prefsMgr, boolean useSystemWP, StatusBarHider sbh,
                             SuggestionsManager suggestionsManager, SuggestionInterface suggestionInterface) {

        super(context, rootView, tri, h, mgr, name, prefsMgr, seeker, useSystemWP, sbh, suggestionsManager, suggestionInterface);

        opener = op;
    }

//    ui abstracts
    @Override
    protected void setupViews(SkinManager skinManager, PreferencesManager preferencesManager, Context c, Handler h, View rootView,
                              boolean inputBottom, boolean showSuggestions) {

        int inputOutputIndex;
        View inputOutputView;
        LayoutInflater inflater = ((Activity) c).getLayoutInflater();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        if(inputBottom) {
            inputOutputView = inflater.inflate(R.layout.input_output_fileview_bottom, null);
            inputOutputIndex = ((ViewGroup) rootView).getChildCount();
        } else {
            inputOutputView = inflater.inflate(R.layout.input_output_fileview_up, null);
            inputOutputIndex = 1;
        }

        ((ViewGroup) rootView).addView(inputOutputView, inputOutputIndex, params);

        input = (EditText) rootView.findViewById(R.id.input_et);
        input.setOnEditorActionListener(this);
        input.setOnTouchListener(this);
        input.setHint(getHint(preferencesManager));
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        if(showSuggestions) {
//            set this for methods of superclass
            suggestionsView = (LinearLayout) ((ViewGroup) inputOutputView.findViewById(R.id.suggestions_id))
                    .getChildAt(0);
//            this throw an exception if null
//            set only here
            input.addTextChangedListener(textWatcher);
        } else
            inputOutputView.findViewById(R.id.suggestions_id).setVisibility(View.GONE);

        separator = (TextView) rootView.findViewById(R.id.separator_tv);

        output = (TextView) rootView.findViewById(R.id.output_tv);
        output.setMovementMethod(new ScrollingMovementMethod());
        output.setOnTouchListener(this);
        tvMgr = new TextViewManager(output, OUTPUT_MILLISECONDS, h);

        Button submit = (Button) rootView.findViewById(R.id.submit_tv);
        if(Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.SHOWSUBMIT))) {
            submit.setOnClickListener(this);
            skinManager.setupSubmit(submit);
        } else
            submit.setVisibility(View.GONE);

        skinManager.setupInput(input);
        skinManager.setupInput(separator);
        skinManager.setupOutput(output);

        fileAdapter = new FileAdapter(c, skinManager);

        showHiddenFiles = Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.SHOW_HIDDENFILES));

        dir = (TextView) rootView.findViewById(R.id.cd_tv);
        skinManager.setupDir(dir);

        GridView dirContent = (GridView) rootView.findViewById(R.id.files_gv);
        dirContent.setOnItemClickListener(this);
        dirContent.setOnItemLongClickListener(this);
        dirContent.setAdapter(fileAdapter);

        int fileColumns;
        try {
            fileColumns = Integer.parseInt(preferencesManager.getValue(PreferencesManager.FILECOLUMNS));
        } catch(NumberFormatException e) {
            fileColumns = FILECOLUMNS_DEFAULT;
        }
        dirContent.setNumColumns(fileColumns);
    }

    @Override
    protected void onSeparatorChanged(String s) {
        separator.setText(s);
    }

    @Override
    public TextViewManager getTvMgr() {
        return tvMgr;
    }

    @Override
    public void clear() {
        input.setText("");
        output.setText("");
    }

    @Override
    public void onResult() {
//        nothing to do
//        separator is immediately changed
//        input & output don't need to be changed here
    }

//    updaters
    public boolean updateDir(File file) {
        dir.setText(file.getAbsolutePath());

        fileAdapter.clear();
        fileAdapter.notifyDataSetChanged();

        ArrayList<File> names;
        try {
            names = FileManager.lsFile(file, showHiddenFiles);
        } catch (NullPointerException exc) {
            return false;
        }

        for(File s : names)
            fileAdapter.add(s);
        fileAdapter.addSuperLabel(file.getParentFile());

        fileAdapter.notifyDataSetChanged();

        return true;
    }

//    on touch
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return verifyDoubleTap(event);

        if (v.getId() == input.getId()) {
            input.onTouchEvent(event);
            processOpenInputRequest();
        } else
        if(!verifyDoubleTap(event))
            output.onTouchEvent(event);
        return true;
    }

//    gridview accessors
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String newInput;
        String currentInput = input.getText().toString();

        String touched = ((File) fileAdapter.getItem(position)).getName();

        newInput = currentInput.equals("") ? touched : currentInput + " " + touched;

        input.setText(newInput);

        canDelete = false;
        focusInputEnd();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File f = (File) fileAdapter.getItem(position);

        opener.onOpenRequest(f);

        return true;
    }
}
