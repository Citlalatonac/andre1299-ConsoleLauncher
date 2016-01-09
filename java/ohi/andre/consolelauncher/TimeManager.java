package ohi.andre.consolelauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import ohi.andre.consolelauncher.managers.SkinManager;
import ohi.andre.consolelauncher.managers.SuggestionsManager;
import ohi.andre.consolelauncher.tuils.interfaces.OnHeadsetStateChangedListener;
import ohi.andre.consolelauncher.tuils.interfaces.PackageListener;
import ohi.andre.consolelauncher.tuils.interfaces.RamUpdater;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionGetter;
import ohi.andre.consolelauncher.tuils.interfaces.SuggestionInterface;

public class TimeManager {


//	  t-ui folder
    private final int FILEUPDATE_DELAY = 300;
    private File tuiFolder;
    private Thread folderThread = new Thread() {

        @Override
        public void run() {
            synchronized (tuiFolder) {
                if (tuiFolder.mkdir() || tuiFolder.isDirectory()) {
                    tuiFolder.notifyAll();
                    return;
                }
            }

            try {
                Thread.sleep(FILEUPDATE_DELAY);
            } catch (InterruptedException e) {
            }
            folderThread.run();
        }
    };
    public void initFolderUpdater(File folder) {
        tuiFolder = folder;
        folderThread.start();
    }


//	  apps
    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String data = intent.getData().getSchemeSpecificPart();
            if (action.equals(Intent.ACTION_PACKAGE_ADDED))
                packageListener.onPackageAdd(data);
            else
                packageListener.onPackageRemoved(data);
        }
    };
    private PackageListener packageListener;
    public void initAppListener(PackageListener p) {
        packageListener = p;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");

        mContext.registerReceiver(appsBroadcast, intentFilter);
    }


//	  music
    private OnHeadsetStateChangedListener ctrl;
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctrl == null)
                return;
            if (intent.getIntExtra("state", 0) != 1)
                ctrl.onHeadsetUnplugged();
        }
    };
    public void initHeadsetListener(OnHeadsetStateChangedListener h) {
        ctrl = h;

        mContext.registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }


//    ram
    private final int RAM_DELAY = 3000;
    private RamUpdater ram;
    private Runnable ramRunnable = new Runnable() {
        @Override
        public void run() {
            ram.onRamUpdate();
            handler.postDelayed(this, RAM_DELAY);
        }
    };
    public void initRamUpdater(RamUpdater r) {
        handler.removeCallbacks(ramRunnable);

        ram = r;
        handler.postDelayed(ramRunnable, RAM_DELAY);
    }


//    suggestions
    private LinearLayout.LayoutParams params;
    private SuggestionInterface suggestionInterface = new SuggestionInterface() {
        @Override
        public void requestSuggestion(final String before, final String lastWord, final SuggestionsManager suggestionsManager,
                                      final ViewGroup rootView, final SuggestionGetter getter) {

            if(params == null) {
                params =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(SkinManager.SUGGESTION_MARGIN, 0, SkinManager.SUGGESTION_MARGIN, 0);
                params.gravity = Gravity.CENTER_VERTICAL;
            }

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    String[] suggestions = suggestionsManager.getSuggestions(before, lastWord);
                    final TextView[] textViews = new TextView[suggestions.length];

                    for(int count = 0; count < textViews.length; count++)
                        textViews[count] = getter.getSuggestionView(suggestions[count], mContext);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(TextView textView : textViews)
                                rootView.addView(textView, params);
                        }
                    });
                }
            }.start();
        }
    };
    public SuggestionInterface getSuggestionInterface() {
        return suggestionInterface;
    }


    private Context mContext;
    private Handler handler;
    public TimeManager(Context c) {
        mContext = c;
    }

    public void init(Handler h) {
        if (handler != null)
            handler.removeCallbacks(ramRunnable);

        if (h == null)
            h = new Handler();

        handler = h;
    }

    public void dispose() {
        handler.removeCallbacks(ramRunnable);
        try {
            mContext.unregisterReceiver(appsBroadcast);
            mContext.unregisterReceiver(headsetReceiver);
        } catch (Exception exc) {
        }
    }

}
