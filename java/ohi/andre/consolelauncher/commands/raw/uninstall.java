package ohi.andre.consolelauncher.commands.raw;

import android.content.Intent;
import android.net.Uri;

import java.io.IOException;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

public class uninstall implements CommandAbstraction {

	@Override
	public String exec(ExecInfo info) throws InterruptedException, IOException {
        String packageName = info.get(String.class, 0);
        if(info.getSu()) {
            try {
                return Tuils.terminalCommand("su pm uninstall " + packageName, null, info.res);
            } catch (Exception e) {}
        }

        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        info.context.startActivity(uninstallIntent);

        return info.res.getString(R.string.output_uninstalling) + " " + packageName;
	}

	@Override
	public int helpRes() {
		return R.string.help_uninstall;
	}

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public int maxArgs() {
		return 1;
	}

	@Override
	public boolean useRealTime() {
		return true;
	}

	@Override
	public int[] argType() {
		return new int[]{CommandAbstraction.PACKAGE};
	}

	@Override
	public int priority() {
		return 3;
	}

	@Override
	public String[] parameters() {
		return null;
	}

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return info.res.getString(helpRes());
    }

	@Override
	public int notFoundRes() {
		return R.string.output_appnotfound;
	}

}
