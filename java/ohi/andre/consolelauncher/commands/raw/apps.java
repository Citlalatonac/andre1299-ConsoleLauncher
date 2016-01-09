package ohi.andre.consolelauncher.commands.raw;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

public class apps implements CommandAbstraction {

    private final String HIDE_PARAM = "-h";
    private final String UNHIDE_PARAM = "-uh";
    private final String SHOWHIDDEN_PARAM = "-sh";

	@Override
	public String exec(ExecInfo info) {
        @SuppressWarnings("unchecked")
        List<String> args = info.get(ArrayList.class, 0);

        String label = args.remove(0);
        String app = Tuils.toPlanString(args, " ");
        if(label.equals(HIDE_PARAM))
            return hideApp(info, app);
        else if(label.equals(UNHIDE_PARAM))
            return unHideApp(info, app);
        else if(label.equals(SHOWHIDDEN_PARAM))
            return showHiddenApps(info);
        else
            return info.res.getString(helpRes());
    }

    private String hideApp(ExecInfo info, String app) {
        SharedPreferences.Editor editor = ((Activity) info.context).getPreferences(0).edit();
        String result = info.appsManager.hideApp(editor, app);
        if(result != null) {
            editor.commit();
            return result + " " + info.res.getString(R.string.output_hideapp);
        }
        else
            return info.res.getString(R.string.output_appnotfound);
    }

    private String unHideApp(ExecInfo info, String app) {
        SharedPreferences.Editor editor = ((Activity) info.context).getPreferences(0).edit();
        String result = info.appsManager.unhideApp(editor, app);
        if(result != null) {
            editor.commit();
            return result + " " + info.res.getString(R.string.output_unhideapp);
        }
        else
            return info.res.getString(R.string.output_appnotfound);
    }

    private String showHiddenApps(ExecInfo info) {
        return info.appsManager.printHiddenApps();
    }

	@Override
	public int helpRes() {
		return R.string.help_apps;
	}

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public int maxArgs() {
		return CommandAbstraction.UNDEFINIED;
	}

	@Override
	public boolean useRealTime() {
		return false;
	}

	@Override
	public int[] argType() {
		return new int[] {CommandAbstraction.TEXTLIST};
	}

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public String[] parameters() {
        return new String[] {
                HIDE_PARAM,
                UNHIDE_PARAM,
                SHOWHIDDEN_PARAM
        };
    }

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return info.appsManager.printApps();
    }

	@Override
	public int notFoundRes() {
		return 0;
	}

}
