package ohi.andre.consolelauncher.commands;

import ohi.andre.consolelauncher.R;

public class Command {
	
	public static int ARG_NOTFOUND = -1;
	
	public CommandAbstraction cmd;
	public Object[] mArgs;
	public int nArgs;
	
	public String exec(ExecInfo info) throws Exception {
		if(nArgs == Command.ARG_NOTFOUND)
			return info.res.getString(cmd.notFoundRes());
		
        if(nArgs < cmd.minArgs())
        	return cmd.onNotArgEnough(info);
		if(cmd.maxArgs() != CommandAbstraction.UNDEFINIED && nArgs > cmd.maxArgs()) 
			return info.res.getString(R.string.output_toomanyargs);
		
		info.set(mArgs);
		
		String output = cmd.exec(info);
		
		info.clear();
		
		return output;
	}
	
	public boolean useRealTimeTyping() {
		return cmd.useRealTime();
	}

	public int nextArg() {
		int[] args = cmd.argType();
		if(args == null)
			return 0;

		if(nArgs == -1)
			nArgs = 0;
		try {
			return args[nArgs];
		} catch (ArrayIndexOutOfBoundsException e) {
			nArgs -= 1;
			return nextArg();
		}
	}
}
