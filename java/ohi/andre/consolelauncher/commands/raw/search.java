package ohi.andre.consolelauncher.commands.raw;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ohi.andre.comparestring.Compare;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

public class search implements CommandAbstraction {

    private final int GOOGLE = 10;
    private final int PLAYSTORE = 11;
    private final int FILE = 12;
    private final int YOUTUBE = 13;

    private final String PLAYSTORE_PARAM = "-p";
    private final String FILE_PARAM = "-f";
    private final String GOOGLE_PARAM = "-g";
    private final String YOUTUBE_PARAM = "-y";

    private final int MIN_FILE_RATE = 4;

	@Override
	public String exec(ExecInfo info) {
		@SuppressWarnings("unchecked")
        List<String> args = info.get(ArrayList.class, 0);

        int type;
		String first = args.get(0);
        if(!first.startsWith("-"))
            type = GOOGLE;
        else {
            if(first.equals(PLAYSTORE_PARAM)) {
                args.remove(0);
                type = PLAYSTORE;
            }
            else if(first.equals(FILE_PARAM)) {
                args.remove(0);
                type = FILE;
            }
            else if(first.equals(YOUTUBE_PARAM)) {
                args.remove(0);
                type = YOUTUBE;
            }
            else if(first.equals(GOOGLE_PARAM)) {
                args.remove(0);
                type = GOOGLE;

            } else
                return info.res.getString(R.string.output_invalid_param);
        }

        if(type == GOOGLE)
            return google(args, info.context, info.res);
        else if(type == PLAYSTORE)
            return playstore(args, info.context, info.res);
        else if(type == FILE)
            return file(args, info.currentDirectory, info.res);
        else if (type == YOUTUBE)
            return youTube(args, info.context, info.res);

        return null;
	}

    private String google(List<String> args, Context c, Resources res) {
        String toSearch = Tuils.toPlanString(args, "+");

        Uri uri = Uri.parse("http://www.google.com/#q=" + toSearch);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        c.startActivity(intent);

        return res.getString(R.string.output_searchinggoogle) + " " + flat(args);
    }

    private String playstore(List<String> args, Context c, Resources res) {
        String toSearch = Tuils.toPlanString(args, "%20");

        try {
            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + toSearch)));
        } catch (android.content.ActivityNotFoundException anfe) {
            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + toSearch)));
        }

        return res.getString(R.string.output_searchingplaystore) + " " + flat(args);
    }

    private String file(List<String> args, File cd, Resources res) {
        String header = res.getString(R.string.output_search_file) + " " + cd.getAbsolutePath();

        String name = Tuils.toPlanString(args);
        String found = Tuils.toPlanString(rightPaths(cd, name), "\n");

        if(found.length() > 1)
            return header.concat("\n" + found);
        else
            return header.concat("\n" + res.getString(R.string.output_nothing_found));
    }

    private List<String> rightPaths(File dir, String name) {
        File[] files = dir.listFiles();
        List<String> rightPaths = new ArrayList<>(files.length);

        for(File file : files) {
            if(fileMatch(file, name))
                rightPaths.add(file.getAbsolutePath());
            if(file.isDirectory())
                rightPaths.addAll(rightPaths(file, name));
        }

        return rightPaths;
    }

    private boolean fileMatch(File f, String name) {
        return Compare.compare(f.getName(), name) >= MIN_FILE_RATE;
    }

    private String youTube(List<String> args, Context c, Resources res) {
        String toSearch = Tuils.toPlanString(args, "+");
        Uri uri = Uri.parse("https://www.youtube.com/results?search_query=" + toSearch);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        c.startActivity(intent);

        return res.getString(R.string.output_search_youtube) + " " + flat(args);
    }

    private String flat(List<String> args) {
        String flat = "";
        for(String s : args)
            flat = flat.concat(s + " ");
        return flat;
    }

	@Override
	public int helpRes() {
		return R.string.help_search;
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
		return true;
	}

	@Override
	public int[] argType() {
		return new int[]{CommandAbstraction.TEXTLIST};
	}

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public String[] parameters() {
        return new String[] {
                PLAYSTORE_PARAM,
                FILE_PARAM,
                GOOGLE_PARAM,
                YOUTUBE_PARAM
        };
    }

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return info.res.getString(helpRes());
    }

	@Override
	public int notFoundRes() {
		return 0;
	}

}
