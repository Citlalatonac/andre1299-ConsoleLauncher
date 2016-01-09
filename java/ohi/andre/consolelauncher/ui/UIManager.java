package ohi.andre.consolelauncher.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.managers.SkinManager;
import ohi.andre.consolelauncher.managers.SuggestionsManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.DirectorySeeker;
import ohi.andre.consolelauncher.tuils.interfaces.StatusBarHider;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionGetter;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionInterface;
import ohi.andre.consolelauncher.tuils.listeners.TrashInterfaces;
import ohi.andre.consolelauncher.ui.fileview.FileViewUIManager;
import ohi.andre.consolelauncher.ui.listview.ListViewUIManager;
import ohi.andre.realtimetyping.TextViewManager;

public abstract class UIManager implements OnEditorActionListener, OnTouchListener, View.OnClickListener {

    private DevicePolicyManager policy;
    private ComponentName component;
    private GestureDetector det;

    protected Context mContext;

    private SkinManager mSkin;

    private InputMethodManager imm;

    protected boolean canDelete = false;
    private boolean intelligentDeletion;

    private CommandExecuter trigger;

    private TextView deviceInfo, ram;

    protected EditText input;
    protected TextView output;

    protected LinearLayout suggestionsView;

    private DirectorySeeker seeker;

    private ActivityManager.MemoryInfo memory;
    private ActivityManager activityManager;

    private SuggestionsManager suggestionsManager;
    private SuggestionInterface suggestionInterface;
    private SuggestionGetter suggestionGetter;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = ((TextView) v).getText().toString();

            String inputText = input.getText().toString();

            int lastSpace = inputText.lastIndexOf(" ");
            String lastWord = inputText.substring(lastSpace != -1 ? lastSpace : 0);
            String before = inputText.substring(0, lastSpace + 1);

            if(lastWord.contains("/")) {
                lastWord = lastWord.substring(0, lastWord.lastIndexOf("/") + 1);
                lastWord = lastWord.concat(s);
            } else
                lastWord = s;

            String newText = before.concat(lastWord);
            input.setText(newText);

            suggestionsView.removeAllViews();

            focusInputEnd();
        }
    };
    protected TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int st, int b, int c) {
            if(suggestionsView == null || suggestionsManager == null)
                return;

            suggestionsView.removeAllViews();

            String text = s.toString();
            int lastSpace = text.lastIndexOf(" ");

            String lastWord = text.substring(lastSpace != -1 ? lastSpace + 1 : 0);
            String before = text.substring(0, lastSpace != -1 ? lastSpace + 1 : 0);

            suggestionInterface.requestSuggestion(before, lastWord, suggestionsManager, suggestionsView, suggestionGetter);
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    public UIManager(Context context, View rootView, CommandExecuter tri, Handler h, DevicePolicyManager mgr, ComponentName name,
                     PreferencesManager prefsMgr, DirectorySeeker seeker, boolean useSystemWP, final StatusBarHider sbh,
                     SuggestionsManager suggestionsManager, SuggestionInterface suggestionInterface) {
        policy = mgr;
        component = name;

        mContext = context;

        trigger = tri;
        this.seeker = seeker;

        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        Typeface lucidaConsole = Typeface.createFromAsset(context.getAssets(), "lucida_console.ttf");
        mSkin = new SkinManager(prefsMgr, lucidaConsole, mContext.getResources(), this instanceof FileViewUIManager,
                suggestionsManager != null);

        this.suggestionsManager = suggestionsManager;
        this.suggestionInterface = suggestionInterface;
        this.suggestionGetter = new SuggestionGetter() {
            @Override
            public TextView getSuggestionView(String text, Context context) {
                TextView textView = new TextView(mContext);
                textView.setText(text);
                textView.setOnClickListener(clickListener);
                mSkin.setupSuggestion(textView);
                return textView;
            }
        };

        if(!useSystemWP)
            mSkin.setupBg(rootView);

        boolean showDevice = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.SHOWDEVICE));
        deviceInfo = (TextView) rootView.findViewById(R.id.deviceinfo_tv);
        if(showDevice) {
            setDeviceName(prefsMgr);
            mSkin.setupDeviceInfo(deviceInfo);
        } else {
            deviceInfo.setVisibility(View.GONE);
            deviceInfo = null;
        }

        boolean showRam = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.SHOWRAM));
        ram = (TextView) rootView.findViewById(R.id.ram_tv);
        if(showRam) {
            mSkin.setupRam(ram);
            memory = new ActivityManager.MemoryInfo();
            activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        } else {
            ram.setVisibility(View.GONE);
            ram = null;
        }

        if(suggestionsManager != null) {
            if(this instanceof ListViewUIManager)
                suggestionsView = (LinearLayout) ((ViewGroup) rootView.findViewById(R.id.suggestions_id)).getChildAt(0);
            else ;
//                done in FileViewUiManager
        } else {
            if(this instanceof ListViewUIManager)
                rootView.findViewById(R.id.suggestions_id).setVisibility(View.GONE);
            this.suggestionsManager = null;
            this.textWatcher = null;
        }

//        i don't want intelligent deletion in listview
        intelligentDeletion = !(this instanceof ListViewUIManager) && Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.USE_INTELLIGENTDELETION));

        boolean closeOnDbTap = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.DOUBLETAP));
        if(!closeOnDbTap) {
            policy = null;
            component = null;
            det = null;
            return;
        }

        initDetector();

        boolean inputBottom = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.INPUTFIELD_BOTTOM));

        setupViews(mSkin, prefsMgr, context, h, rootView, inputBottom, suggestionsManager != null);
        onSeparatorChanged(context.getString(R.string.separator_text));

//        called to add the firstview in listview type
        onResult();

//        remove unecessary references
        if(suggestionsManager == null)
            mSkin = null;
    }

//	 on enter listener
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId != EditorInfo.IME_ACTION_GO && actionId != EditorInfo.IME_ACTION_DONE)
            return false;

        onInput();

        return true;
    }

    private void onInput() {
        if(input.getText().length() == 0)
            return;

        canDelete = intelligentDeletion && !(this instanceof ListViewUIManager);

        if(!(this instanceof ListViewUIManager))
            closeKeyboard();

        trigger.exec(input.getText().toString());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.submit_tv)
            onInput();
    }

    //	 keyboard managers
    protected void openKeyboard() {
        if(input != null) {
            input.requestFocus();
            imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
        }
    }

    private void closeKeyboard() {
        if(input != null)
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

//   accessor for input
    public void setInput(String s) {
        input.setText(s);
        canDelete = false;
        focusInputEnd();
    }

//   accessor for output
    public void setOutput(String string, boolean realTime) {
        if(output == null || string == null || string.equals(""))
            return;

        output.scrollTo(0, 0);
        output.setText("");

        if(realTime)
            realTime = !(this instanceof ListViewUIManager);

        if(realTime) {
            TextViewManager tvMgr = getTvMgr();
            if(tvMgr != null)
                tvMgr.newText(string);
        } else
            output.setText(string);
    }

//    accessor for separator (for root "#")
    public void setSeparator(String text) {
        onSeparatorChanged(text);
    }

//    get device name
    private void setDeviceName(PreferencesManager preferencesManager) {
        String name = preferencesManager.getValue(PreferencesManager.DEVICENAME);
        if(name.length() == 0 || name.equals("null"))
            deviceInfo.setText(Build.MODEL);
        else
            deviceInfo.setText(name);
    }

//    update ram
    public void updateRamDetails() {
        ram.setText("RAM: " + Tuils.ramDetails(activityManager, memory));
    }

//	 get hint for input
    protected String getHint(PreferencesManager preferencesManager) {
        boolean cdPath = !Boolean.parseBoolean(preferencesManager.getValue(PreferencesManager.SHOWUSERNAME));

        String hint;

        if(cdPath)
            hint = seeker.readDirectoryPath();
        else {
            hint = preferencesManager.getValue(PreferencesManager.USERNAME);
            if(hint == null || hint.length() == 0) {
                String email = Tuils.getUsername(mContext);
                if(email != null) {
                    if (email.endsWith("@gmail.com"))
                        email = email.substring(0, email.length() - 10);
                    return "user@".concat(email);
                } else
                    return Tuils.getSDK();
            } else
                hint = "user@".concat(hint);
        }
        return hint;
    }

//    focus input end
    protected void focusInputEnd() {
        input.setSelection(input.getText().length());
    }

//    on input event
    protected void processOpenInputRequest() {
        openKeyboard();
        if(!canDelete)
            return;
        input.setText("");
        canDelete = false;
    }

//	 init detector for double tap
    private void initDetector() {
        det = new GestureDetector(mContext, TrashInterfaces.trashGestureListener);

        det.setOnDoubleTapListener(new OnDoubleTapListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                boolean admin = policy.isAdminActive(component);

                if (!admin)
                    Tuils.requestAdmin((Activity) mContext, component,
                            mContext.getString(R.string.adminrequest_label));
                else
                    policy.lockNow();

                return true;
            }
        });
    }

    protected boolean verifyDoubleTap(MotionEvent event) {
        return det != null && det.onTouchEvent(event);
    }

//	 on pause
    public void pause() {
        closeKeyboard();
    }


//    abstracts
    protected abstract void setupViews(SkinManager skinManager, PreferencesManager preferencesManager, Context c, Handler h, View rootView,
                                       boolean inputBottom, boolean showSuggestions);

    protected abstract TextViewManager getTvMgr();

    protected abstract void onSeparatorChanged(String s);
    public abstract void onResult();

    public abstract void clear();
}

