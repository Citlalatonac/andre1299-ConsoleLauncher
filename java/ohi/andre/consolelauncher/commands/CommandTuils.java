package ohi.andre.consolelauncher.commands;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.FileManager.DirInfo;
import ohi.andre.consolelauncher.managers.MusicManager;
import ohi.andre.consolelauncher.tuils.Tuils;

@SuppressLint("DefaultLocale")
public class CommandTuils {

//	parse a command
	public static Command parse(String input, ExecInfo info) throws Exception {
		Command command = new Command();
		
		input = Tuils.trimSpaces(input);

		if(input.startsWith("su ")) {
			info.setSu(Tuils.verifyRoot());
			input = input.substring(3);
		}

		String name = CommandTuils.findName(input);
		if(!Tuils.isAlpha(name))
			return null;
	
		CommandAbstraction cmd = info.active.getCommandByName(name);
		if(cmd == null)
			return null;
		command.cmd = cmd;

		input = input.substring(name.length());

		int[] types = cmd.argType();
		ArrayList<Object> args = new ArrayList<>();
		int nArgs = 0;

		if(types != null) {
			for (int type : types) {
				input = Tuils.trimSpaces(input);
				if (input.length() <= 0)
					break;

				ArgInfo arg = CommandTuils.getArg(info, input, type);

				if (!arg.found) {
					command.nArgs = Command.ARG_NOTFOUND;
					return command;
				}

				nArgs += arg.n;
				args.add(arg.arg);
				input = arg.residualString;
			}
		}
		
		command.mArgs = args.toArray(new Object[args.size()]);
		command.nArgs = nArgs;
		
        return command;
	}
	
//	find command name
	private static String findName(String input) {
		int space = input.indexOf(" ");
		
		input = input.toLowerCase();
		
		if(space == -1)
			return input; 
		else
			return input.substring(0, space);
	}

//	find args
	public static ArgInfo getArg(ExecInfo info, String input, int type) {
		if (type == CommandAbstraction.FILE)
			return CommandTuils.file(input, info.currentDirectory);
		else if (type == CommandAbstraction.CONTACTNUMBER)
			return CommandTuils.contactNumber(input, info.contacts);
//        will always find a plain text
        else if (type == CommandAbstraction.PLAIN_TEXT)
			return CommandTuils.plainText(input);
		else if (type == CommandAbstraction.PACKAGE)
			return CommandTuils.packageName(input, info.appsManager);
//        will always find a textlist
		else if (type == CommandAbstraction.TEXTLIST)
			return CommandTuils.textList(input);
		else if (type == CommandAbstraction.SONG)
			return CommandTuils.song(input, info.player);
		else if (type == CommandAbstraction.FILE_LIST)
			return CommandTuils.fileList(input, info.currentDirectory);
		else if (type == CommandAbstraction.COMMAND)
			return CommandTuils.command(input, info.active);
		return null;
	}
	
	
//	args extractors {
	
	private static ArgInfo plainText(String input) {
		return new ArgInfo(input, "", true, 1);
	}
	
	private static ArgInfo textList(String input) {
		List<String> strings = new ArrayList<>();
		
		char[] chars = input.toCharArray();
		String arg = "";
		int index;
		
        for (index = 0; index < chars.length; index++) {
        	char c = chars[index];
            if (c == ' ') {
                if(arg.length() > 0) {
                    strings.add(arg);
                    arg = "";
                    continue;
                } else {
//                	prevent double space
                	continue;
                }
            }

            arg = arg.concat(c + "");
        }
		
		if(arg.length() > 0) 
			strings.add(arg);
		
		return new ArgInfo(strings, input.substring(index), true, strings.size());
	}

	private static ArgInfo command(String string, CommandGroup active) {
		CommandAbstraction abstraction = null;
		try {
			abstraction = active.getCommandByName(string);
		} catch (Exception e) {}

		return new ArgInfo(abstraction, null, abstraction != null, 1);
	}
	
	@SuppressWarnings("unchecked")
	private static ArgInfo file(String input, File cd) {
		List<String> strings = (List<String>) CommandTuils.textList(input).arg;
		
		String toVerify = "";
		for(int count = 0; count < strings.size(); count++) {
			toVerify = toVerify.concat(strings.get(count));
			
			DirInfo info = CommandTuils.getFile(toVerify, cd);
			if(info.file != null && info.notFound == null) {
				while(count-- >= 0)
					strings.remove(0);
				
				String residual = Tuils.toPlanString(strings, " ");
				return new ArgInfo(info.file, residual, true, 1);
			}
			
			toVerify = toVerify.concat(" ");
		}
		
		return new ArgInfo(null, input, false, 0);
	}
	
	@SuppressWarnings("unchecked")
	private static ArgInfo fileList(String input, File cd) {
        List<File> files = new ArrayList<>();
		List<String> strings = (List<String>) CommandTuils.textList(input).arg;
		
		String toVerify = "";
		for(int count = 0; count < strings.size(); count++) {
			String s = strings.get(count);
			
			toVerify = toVerify.concat(s);
			
			DirInfo dir = CommandTuils.getFile(toVerify, cd);
			if(dir.notFound == null) {
				files.add(dir.file);
				
				toVerify = "";
				continue;
			}
			
			List<File> tempFiles = CommandTuils.attemptWildcard(dir, cd);
			if(tempFiles != null) {
				files.addAll(tempFiles);
				
				toVerify = "";
				continue;
			}
			
			toVerify = toVerify.concat(" ");
		}
		
		if(toVerify.length() > 0) 
			return new ArgInfo(null, null, false, 0);
		
		return new ArgInfo(files, null, files.size() > 0, files.size());
	}
	
	private static DirInfo getFile(String path, File cd) {
		return FileManager.cd(cd, path);
	}

	private static List<File> attemptWildcard(DirInfo dir, File cd) {
		List<File> files;
		final String extension = FileManager.wildcard(dir.notFound);
		if(extension == null)
			return null;

		if(extension.equals(FileManager.ALL)) 
			files = Arrays.asList(cd.listFiles());
		else {
			FileFilter filter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					if(f.isDirectory())
						return false;

					String name = f.getName();
					if(!name.contains("."))
						return false;

					String fileExtension = name.substring(name.lastIndexOf("."));

                    return !(!fileExtension.equals(extension.toLowerCase()) &&
                            !fileExtension.equals(extension.toUpperCase()));

                }
			};

			files = Arrays.asList(cd.listFiles(filter));
		}
		
		if(files.size() > 0)
			return files;
		return null;
	}
	
//	cant use more args
	private static ArgInfo packageName(String input, AppsManager apps) {
		String packageName = apps.findPackage(apps.getApps(), input);
        return new ArgInfo(packageName, null, packageName != null, 1);
	}
	
//	cant use more args
	private static ArgInfo contactNumber(String input, ContactManager contacts) {
		String number;
		
		if(Tuils.isNumber(input))
			number = input;
		else
            number = contacts.findNumber(input);

        return new ArgInfo(number, null, number != null, 1);
	}
	
//	cant use more args
	private static ArgInfo song(String input, MusicManager music) {
		String song = music.getSong(input);
		return new ArgInfo(song, null, song != null, 1);
	}
	
//	} arg estractors
	
//	is SU request?
	public static boolean isSuRequest(String input) {
		input = Tuils.trimSpaces(input);
		return input.equals("su");
	}

//	is su command?
	public static boolean isSuCommand(String input) {
		return input.startsWith("su ");
	}
	
	public static class ArgInfo {
		public Object arg;
		public String residualString;
		public int n;
		public boolean found;
		
		public ArgInfo(Object a, String s, boolean f, int i) {
			this.arg = a;
			this.residualString = s;
			this.found = f;
			this.n = i;
		}
	}
	
}
