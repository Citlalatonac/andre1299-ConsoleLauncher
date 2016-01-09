package ohi.andre.consolelauncher.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ohi.andre.comparestring.Compare;
import ohi.andre.consolelauncher.tuils.AppInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

public class AppsManager {
	
	public static final String APP_LABEL = "label";

    private static final String HIDDENAPP_KEY = "hiddenapp_";
    private static final String HIDDENAPP_N_KEY = "hiddenapp_n";

    private final int MIN_RATE = 5;
    
    private Random random;
	
	private PackageManager mgr;

	private Set<AppInfo> apps;
    private Set<AppInfo> hiddenApps;

//    constructor
	public AppsManager(Context context, Random random) {
		mgr = context.getPackageManager();

		fill(((Activity) context).getPreferences(0));
		
		this.random = random;
	}

//    put apps and hiddenapps
	public void fill(SharedPreferences preferences) {
		apps = AppsManager.getApps(mgr);

        Set<String> hiddenPackages = getHiddenApps(preferences);
        hiddenApps = new HashSet<>();

//        remove hidden apps from apps & store coincidences in hiddenApps
        Iterator<AppInfo> it = apps.iterator();
        while (it.hasNext()) {
            AppInfo app = it.next();
            if(hiddenPackages.contains(app.packageName)) {
                hiddenApps.add(app);
                it.remove();
            }
        }

        apps = checkEquality(apps);
        hiddenApps = checkEquality(hiddenApps);
	}

//    add a new app (onPackageAdded listener)
	public void add(String packageName) {
		try {
			ApplicationInfo info = mgr.getApplicationInfo(packageName, 0);
            AppInfo app = new AppInfo(packageName, info.loadLabel(mgr).toString());
			apps.add(app);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

        apps = checkEquality(apps);
	}

//    as below, but remove
	public void remove(String packageName) {
        Iterator<AppInfo> it = apps.iterator();

        AppInfo app;
        while (it.hasNext()) {
            app = it.next();
			if(app.packageName.equals(packageName)) {
				it.remove();
				break;
			}
		}
	}

//    find a package using its public label
//    notice that it can be an app or an hidden app
    public String findPackage(Set<AppInfo> appList, String name) {
        if(packageSet(appList).contains(name))
            return name;

        String label = Compare.compare(labelSet(appList), name, MIN_RATE, random);
        if(label == null)
            return null;

        Iterator<AppInfo> it = appList.iterator();
        AppInfo app;
        while (it.hasNext()) {
            app = it.next();
            if(app.publicLabel.equals(label))
                return app.packageName;
        }

        return null;
    }

//    print a list of shown apps
    public String printApps() {
        List<String> list = new ArrayList<>(labelSet(apps));

        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return Compare.alphabeticCompare(lhs, rhs);
            }
        });

        Tuils.addPrefix(list, "  ");
        Tuils.insertHeaders(list);
        return Tuils.toPlanString(list);
    }

//    return hidden apps
    public String printHiddenApps() {
        List<String> list = new ArrayList<>(labelSet(hiddenApps));

        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return Compare.alphabeticCompare(lhs, rhs);
            }
        });

        Tuils.addPrefix(list, "  ");
        Tuils.insertHeaders(list);
        return Tuils.toPlanString(list);
    }

//    return apps
    public Set<AppInfo> getApps() {
        return new HashSet<>(apps);
    }

//    find the Application intent
	public Intent getIntent(String input) {
		String packageName = findPackage(apps, input);
        if(packageName == null)
            return null;

        Intent i = mgr.getLaunchIntentForPackage(packageName);
		i.putExtra(AppsManager.APP_LABEL, packageName);
		
		return i;
	}

//    retrieve a list of installed apps
	private static Set<AppInfo> getApps(PackageManager mgr) {
		Set<AppInfo> set = new HashSet<>();
		
		Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos =  mgr.queryIntentActivities(i, 0);

        AppInfo app;
		for(ResolveInfo info : infos) {
            app = new AppInfo(info.activityInfo.packageName, info.loadLabel(mgr).toString());
			set.add(app);
		}
		
		return set;
	}

//    read hidden apps
    private Set<String> getHiddenApps(SharedPreferences preferences) {
        int n = preferences.getInt(HIDDENAPP_N_KEY, 0);

        Set<String> hiddenPackages = new HashSet<>();
        for(int count = 0; count < n; count++)
            hiddenPackages.add(preferences.getString(HIDDENAPP_KEY + count, null));
        return hiddenPackages;
    }

//    store hidden apps in shared preferences
    private void storeHiddenApps(SharedPreferences.Editor editor, Set<String> hiddenPackages) {
        int n = hiddenPackages.size();

        editor.putInt(HIDDENAPP_N_KEY, n);

        Iterator<String> it = hiddenPackages.iterator();
        for(int count = 0; count < n; count++)
            editor.putString(HIDDENAPP_KEY + count, it.next());
    }

//    hide an app
    public String hideApp(SharedPreferences.Editor editor, String appLabel) {
        String packageName = findPackage(apps, appLabel);
        if(packageName == null)
            return null;

        Iterator<AppInfo> it = apps.iterator();
        while(it.hasNext()) {
            AppInfo i = it.next();
            if(i.packageName.equals(packageName)) {
                it.remove();
                hiddenApps.add(i);
                hiddenApps = checkEquality(hiddenApps);

                storeHiddenApps(editor, packageSet(hiddenApps));

                return i.publicLabel;
            }
        }

        return null;
    }

//    unhide an app
    public String unhideApp(SharedPreferences.Editor editor, String appLabel) {
        String packageName = findPackage(hiddenApps, appLabel);
        if(packageName == null)
            return null;

        Iterator<AppInfo> it = hiddenApps.iterator();
        while(it.hasNext()) {
            AppInfo i = it.next();
            if(i.packageName.equals(packageName)) {
                it.remove();
                apps.add(i);

                storeHiddenApps(editor, packageSet(hiddenApps));

                return i.publicLabel;
            }
        }

        return null;
    }

//    return a set of labels in AppInfos
    private Set<String> labelSet(Set<AppInfo> infos) {
        Set<String> set = new HashSet<>();

        Iterator<AppInfo> it = infos.iterator();
        while(it.hasNext())
            set.add(it.next().publicLabel);

        return set;
    }

//    return a set of packages in AppInfos
    private Set<String> packageSet(Set<AppInfo> infos) {
        Set<String> set = new HashSet<>();

        Iterator<AppInfo> it = infos.iterator();
        while(it.hasNext())
            set.add(it.next().packageName);

        return set;
    }

    private Set<AppInfo> checkEquality(Set<AppInfo> appInfoSet) {
        List<AppInfo> list = new ArrayList<>(appInfoSet);

        for(Iterator<AppInfo> iterator = appInfoSet.iterator(); iterator.hasNext(); ) {
            AppInfo info = iterator.next();

            for(int count = 0; count < list.size(); count++) {
                AppInfo info2 = list.get(count);
                if(!info.equals(info2) && info.publicLabel.equals(info2.publicLabel))
                    list.set(count, new AppInfo(info2.packageName, getNewLabel(info2.publicLabel, info2.packageName)));
            }
        }

        return new HashSet<>(list);
    }

    private String getNewLabel(String oldLabel, String packageName) {
        int firstDot = packageName.indexOf(".") + 1;
        int secondDot = packageName.substring(firstDot).indexOf(".") + firstDot;
        String newLabel = packageName.substring(firstDot, secondDot == -1 ? packageName.length() : secondDot).concat(" ").concat(oldLabel);
        return newLabel.substring(0, 1).toUpperCase() + newLabel.substring(1);
    }

}
