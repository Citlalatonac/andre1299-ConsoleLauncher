package ohi.andre.consolelauncher.commands.raw;

import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

public class peoples implements CommandAbstraction {

	@Override
	public String exec(ExecInfo info) {
		List<String> contacts = info.contacts.list();
		Tuils.insertHeaders(contacts);
		return Tuils.toPlanString(contacts);
	}

	@Override
	public int helpRes() {
		return R.string.help_contacts;
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
