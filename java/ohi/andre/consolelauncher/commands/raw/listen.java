package ohi.andre.consolelauncher.commands.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.commands.generals.music;

public class listen extends music {

	@Override
	public String exec(ExecInfo info) {
        String output = super.exec(info);
        if(output != null) 
            return output;

        String song = info.get(String.class, 0);
        if(info.player.jukebox(song))
            return info.res.getString(R.string.output_playing) + " " + song;
        return info.res.getString(R.string.output_nothingfound);
	}

	@Override
	public int helpRes() {
		return R.string.help_listen;
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
	public int[] argType() {
		return new int[]{CommandAbstraction.SONG};
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
        return info.res.getString(helpRes());
    }

	@Override
	public int notFoundRes() {
		return R.string.output_nothingfound;
	}

}
