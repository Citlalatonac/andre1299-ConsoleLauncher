package ohi.andre.consolelauncher.ui.fileview;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

import java.io.File;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.tuils.interfaces.Clearer;
import ohi.andre.consolelauncher.tuils.interfaces.DirectoryUpdater;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.OnDirChangedListener;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;

public class FileViewMainManager extends ohi.andre.consolelauncher.ui.MainManager {

    private OnDirChangedListener dir;

	public FileViewMainManager(LauncherActivity c, OnDirChangedListener d, Inputable i, Outputable o, PreferencesManager prefsMgr,
							   DevicePolicyManager devicePolicyManager, ComponentName componentName, Clearer clearer) {
        super(c, i, o, prefsMgr, devicePolicyManager, componentName, clearer);

		this.dir = d;

        getInfo().dirUpdater = new DirectoryUpdater() {

            @Override
            public void update() {
                requestDirUpdate();
            }
        };

        requestDirUpdate();
	}

	public boolean requestDirUpdate() {
        return dir.onChange(getInfo().currentDirectory);
	}
	
	public void onExternalInput(File f) {
		if(!f.exists())
			return;

		String command;
		if(f.isDirectory())
			command = "cd ";
		else
			command = "open ";
		command = command.concat(f.getAbsolutePath());

		onCommand(command);
	}

}
