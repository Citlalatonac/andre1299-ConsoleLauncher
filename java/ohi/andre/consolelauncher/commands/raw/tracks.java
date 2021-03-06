package ohi.andre.consolelauncher.commands.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by andre on 07/08/15.
 */

public class tracks implements CommandAbstraction {

    @Override
    public String exec(ExecInfo info) {
        return Tuils.toPlanString(info.player.getNames());
    }

    @Override
    public int helpRes() {
        return R.string.help_tracks;
    }

    @Override
    public boolean useRealTime() {
        return false;
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 0;
    }

    @Override
    public int[] argType() {
        return null;
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
