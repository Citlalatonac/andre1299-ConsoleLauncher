package ohi.andre.consolelauncher.commands.raw;

import java.io.File;
import java.io.IOException;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.managers.FileManager;

public class install implements CommandAbstraction {

	@Override
	public String exec(ExecInfo info) throws InterruptedException, IOException {
		File f = info.get(File.class, 0);
		if(!(f.getName().endsWith(".apk") && !f.getName().endsWith(".APK")))
            return info.res.getString(R.string.output_invalidapk);

        int output = FileManager.openFile(info.context, f);
        if(output == FileManager.ISDIRECTORY)
            return info.res.getString(R.string.output_isdirectory);

        return info.res.getString(R.string.output_installing) + " " + f.getName();
	}

	@Override
	public int helpRes() {
		return R.string.help_install;
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
		return new int[]{CommandAbstraction.FILE};
	}

	@Override
	public int priority() {
		return 2;
	}

	@Override
	public String[] parameters() {
		return null;
	}

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return null;
    }

	@Override
	public int notFoundRes() {
		return 0;
	}

}
