package ohi.andre.consolelauncher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import ohi.andre.consolelauncher.managers.ChangelogManager;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.managers.SuggestionsManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Clearer;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;
import ohi.andre.consolelauncher.tuils.interfaces.DirectorySeeker;
import ohi.andre.consolelauncher.tuils.interfaces.FileOpener;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.OnDirChangedListener;
import ohi.andre.consolelauncher.tuils.interfaces.OnHeadsetStateChangedListener;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;
import ohi.andre.consolelauncher.tuils.interfaces.PackageListener;
import ohi.andre.consolelauncher.tuils.interfaces.RamUpdater;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;
import ohi.andre.consolelauncher.tuils.interfaces.StatusBarHider;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionInterface;
import ohi.andre.consolelauncher.tuils.listeners.PolicyReceiver;
import ohi.andre.consolelauncher.ui.MainManager;
import ohi.andre.consolelauncher.ui.UIManager;
import ohi.andre.consolelauncher.ui.fileview.FileViewMainManager;
import ohi.andre.consolelauncher.ui.fileview.FileViewUIManager;
import ohi.andre.consolelauncher.ui.listview.ListViewMainManager;
import ohi.andre.consolelauncher.ui.listview.ListViewUIManager;

public class LauncherActivity extends Activity implements Reloadable {

    private final String TUI_FOLDER = "t-ui";
    private final String FIRSTACCESS_KEY = "firstAccess";

    private UIManager ui;
    private TimeManager time;
    private MainManager main;

    private boolean hideStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getPreferences(0);
        boolean firstAccess = preferences.getBoolean(FIRSTACCESS_KEY, true);
        if(firstAccess) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRSTACCESS_KEY, false);
            editor.commit();

            Tuils.showTutorial(this);
        }

        Resources res = getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermission(res);
        if (isFinishing())
            return;

        Handler mainHandler = new Handler();
        DevicePolicyManager policy = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName component = new ComponentName(this, PolicyReceiver.class);

        initTime(mainHandler);

        File tuiFolder = getFolder();

        ChangelogManager changelogManager = new ChangelogManager(tuiFolder);
        try {
            if(changelogManager.needUpdate())
                changelogManager.write();
        }
        catch (NullPointerException e) {
            try {
                changelogManager.write();
            } catch (IOException e1) {}
        }
        catch (Exception e) {}

        PreferencesManager prefsMgr = null;
        try {
            prefsMgr = new PreferencesManager(res, tuiFolder);
        } catch (IOException e) {}

        if(Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.SHOWRAM)))
            time.initRamUpdater(new RamUpdater() {

                @Override
                public void onRamUpdate() {
                    try {
                        ui.updateRamDetails();
                    } catch (NullPointerException e) {}
                }
            });

//        use system wp
        boolean useSystemWP = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.USE_SYSTEMWP));
        if (useSystemWP)
            setTheme(R.style.SystemWallpaperStyle);

//        hide status bar
        hideStatusBar = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.FULLSCREEN));

//        transparent statusBar
        boolean transparentStatusBar = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.TRANSPARENT_STATUSBAR));
        if (transparentStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

//        show file mgr
        boolean showFileManager = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.SHOW_FILEMANAGER));
        int layout = showFileManager ? R.layout.activity_launcher_fileview : R.layout.activity_launcher_listview;
        setContentView(layout);

        View mainView = findViewById(R.id.mainview);
        initMainMgr(showFileManager, prefsMgr, policy, component);
        initUI(showFileManager, mainHandler, mainView, policy, component, prefsMgr, useSystemWP);

        if(main instanceof FileViewMainManager)
            ((FileViewMainManager) main).requestDirUpdate();

        System.gc();
    }

    private void initTime(Handler h) {
        time = new TimeManager(this);
        time.init(h);
        time.initAppListener(new PackageListener() {

            @Override
            public void onPackageRemoved(String rem) {
                try {
                    main.onRemApp(rem);
                } catch (NullPointerException e) {
                }
            }

            @Override
            public void onPackageAdd(String add) {
                try {
                    main.onAddApp(add);
                } catch (NullPointerException e) {
                }
            }
        });
        time.initHeadsetListener(new OnHeadsetStateChangedListener() {

            @Override
            public void onHeadsetUnplugged() {
                try {
                    main.onHeadsetUnplugged();
                } catch (NullPointerException e) {
                }
            }
        });
    }

    private void initUI(boolean showFileMgr, Handler h, View v, DevicePolicyManager po, ComponentName name, PreferencesManager prefs,
                        boolean useSystemWP) {

        CommandExecuter ex = new CommandExecuter() {

            @Override
            public void exec(String input) {
                try {
                    main.onCommand(input);
                } catch (NullPointerException e) {
                }
            }
        };

        FileOpener op = new FileOpener() {

            @Override
            public void onOpenRequest(File f) {
                try {
                    if (main instanceof FileViewMainManager)
                        ((FileViewMainManager) main).onExternalInput(f);
                } catch (NullPointerException e) {
                }
            }
        };

        DirectorySeeker seeker = new DirectorySeeker() {

            @Override
            public String readDirectoryPath() {
                try {
                    return main.getCurrentDirectory().getAbsolutePath();
                } catch (NullPointerException e) {
                    return null;
                }
            }
        };

        StatusBarHider hider = new StatusBarHider() {

            @Override
            public void onHideRequest() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hideStatusBar();
                    }
                });
            }
        };

        boolean showSuggestions = Boolean.parseBoolean(prefs.getValue(PreferencesManager.SHOWSUGGESTIONS));
        SuggestionsManager suggestionsManager = null;
        SuggestionInterface suggestionInterface = null;
        if(showSuggestions) {
            suggestionsManager = new SuggestionsManager(main.getInfo());
            suggestionInterface = time.getSuggestionInterface();
        }

        if (showFileMgr)
            ui = new FileViewUIManager(this, v, ex, op, h, po, name, seeker, prefs, useSystemWP, hider, suggestionsManager, suggestionInterface);
        else
            ui = new ListViewUIManager(this, v, ex, h, po, name, prefs, seeker, useSystemWP, hider, suggestionsManager, suggestionInterface);
    }

    private void initMainMgr(boolean showFileMgr, PreferencesManager prefs, DevicePolicyManager po, ComponentName name) {
        Inputable in = new Inputable() {

            @Override
            public void in(String s) {
                try {
                    ui.setInput(s);
                } catch (NullPointerException e) {
                }
            }
        };

        final Outputable out = new Outputable() {

            @Override
            public void onOutput(String output, boolean realTime) {
                try {
                    ui.setOutput(output, realTime);
                    ui.onResult();
                } catch (NullPointerException e) {
                }
            }
        };

        Clearer clearer = new Clearer() {

            @Override
            public void clear() {
                ui.clear();
            }
        };

        if (showFileMgr) {
            OnDirChangedListener dir = new OnDirChangedListener() {

                @Override
                public boolean onChange(File file) {
                    try {
                        return ui instanceof FileViewUIManager && ((FileViewUIManager) ui).updateDir(file);
                    } catch (NullPointerException e) {
                        return false;
                    }
                }
            };

            main = new FileViewMainManager(this, dir, in, out, prefs, po, name, clearer);
        } else
            main = new ListViewMainManager(this, in, out, prefs, po, name, clearer);
    }

    private File getFolder() {
        File tuiFolder = new File(Environment.getExternalStorageDirectory(), TUI_FOLDER);
        time.initFolderUpdater(tuiFolder);

        synchronized (tuiFolder) {
            try {
                tuiFolder.wait();
            } catch (InterruptedException e) {
            }
        }

        return tuiFolder;
    }

    private void hideStatusBar() {
        if (!hideStatusBar)
            return;

        if (Build.VERSION.SDK_INT < 16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @TargetApi(23)
    private void checkPermission(Resources res) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.finish();
            Tuils.openSettingsPage(this, res.getString(R.string.permissions_toast));
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            this.finish();
            Tuils.openSettingsPage(this, res.getString(R.string.permissions_toast));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideStatusBar();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ui != null && main != null) {
            ui.pause();
            main.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (time != null)
            time.dispose();
    }

    @Override
    public void onBackPressed() {
        if (main != null)
            main.onBackPressed();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK)
            return super.onKeyLongPress(keyCode, event);

        if (main != null)
            main.onLongBack();
        return true;
    }

    @Override
    public void reload() {
        Intent intent = getIntent();
        startActivity(intent);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus)
            hideStatusBar();
    }
}
