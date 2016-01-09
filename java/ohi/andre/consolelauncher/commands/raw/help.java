package ohi.andre.consolelauncher.commands.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;

public class help implements CommandAbstraction {

	@Override
	public String exec(ExecInfo info) throws Exception {
		CommandAbstraction cmd = info.get(CommandAbstraction.class, 0);
		int res = cmd == null ? R.string.output_commandnotfound : cmd.helpRes();
		return info.res.getString(res);
	}
	
	@Override
	public int helpRes() {
		return R.string.help_help;
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
		return false;
	}

	@Override
	public int[] argType() {
		return new int[]{CommandAbstraction.COMMAND};
	}

	@Override
	public int priority() {
		return 5;
	}

	@Override
	public String[] parameters() {
		return null;
	}

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return info.active.printCommands();
    }

	@Override
	public int notFoundRes() {
		return R.string.output_commandnotfound;
	}

}
