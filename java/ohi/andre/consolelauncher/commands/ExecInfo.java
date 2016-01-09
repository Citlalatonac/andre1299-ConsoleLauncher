package ohi.andre.consolelauncher.commands;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.File;
import java.lang.reflect.Method;

import ohi.andre.consolelauncher.managers.AliasManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.MusicManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Clearer;
import ohi.andre.consolelauncher.tuils.interfaces.DirectoryUpdater;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

@SuppressWarnings("deprecation")
public class ExecInfo {

//	current directory
	public File currentDirectory;
	public DirectoryUpdater dirUpdater;
	
//	current set of commands
	public CommandGroup active;
	
//	context for accessing methods
	public Context context;
	
//	resources references
	public Resources res;
	
//	flashlight
	public boolean isFlashOn = false, canUseFlash = false;
	public Camera camera;
	public Parameters parameters;
	
//	internet
	public WifiManager wifi;

//	3g/data
	public Method setMobileDataEnabledMethod;
	public ConnectivityManager connectivityMgr;
	public Object connectMgr;
	
//	contacts
	public ContactManager contacts;
	
//	music
	public MusicManager player;
	
//	exploring apps & assocs
	public AliasManager aliasManager;
	public AppsManager appsManager;
	
//	admin
	public DevicePolicyManager policy;
	public ComponentName component;
	
//	reload field
	public Reloadable reloadable;
	
//	current set of args
	private Object[] args;
	
//	uses su
	private boolean canUseSu = false;

	public boolean getSu() {
		boolean su = canUseSu;
		canUseSu = false;
		return su;
	}

	public void setSu(boolean su) {
		this.canUseSu = su;
	}

//  clear
    public Clearer clearer;
	
	public ExecInfo(Context context, CommandGroup active, AliasManager alMgr, AppsManager appmgr, MusicManager p,
					ContactManager c, DevicePolicyManager devicePolicyManager, ComponentName componentName,
					Reloadable r, Clearer cl) {
		this.res = context.getResources();
		this.active = active;
		
		this.context = context;

		this.currentDirectory = new File(Tuils.getInternalDirectoryPath());
		this.aliasManager = alMgr;
		this.appsManager = appmgr;

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1)
			initFlash(context);

		this.player = p;
		this.contacts = c;

		this.policy = devicePolicyManager;
		this.component = componentName;
		
		this.reloadable = r;
		
        this.clearer = cl;
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
	private void initFlash(Context context) {
		this.canUseFlash = context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}

	public void initCamera() {
		this.camera = Camera.open();
		this.parameters = this.camera.getParameters();
	}
	
	public void dispose() {
		if(this.camera == null || this.isFlashOn)
			return;
		
		this.camera.stopPreview();
		this.camera.release();
		this.camera = null;
		this.parameters = null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> c, int index) {
		return (T) (this.args[index]);
	}
	
	public void set(Object[] objs) {
		this.args = objs;
	}
	
	public void clear() {
		args = null;
	}
}
