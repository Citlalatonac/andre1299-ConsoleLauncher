package ohi.andre.consolelauncher.ui.listview;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.tuils.interfaces.Clearer;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;
import ohi.andre.consolelauncher.ui.MainManager;

/**
 * Created by andre on 03/11/15.
 */
public class ListViewMainManager extends MainManager {

    public ListViewMainManager(LauncherActivity c, Inputable i, Outputable o, PreferencesManager prefsMgr,
                               DevicePolicyManager devicePolicyManager, ComponentName componentName, Clearer clearer) {
        super(c, i, o, prefsMgr, devicePolicyManager, componentName, clearer);
    }
}
