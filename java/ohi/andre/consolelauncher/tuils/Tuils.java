package ohi.andre.consolelauncher.tuils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Patterns;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import dalvik.system.DexFile;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.CommandTuils;

public class Tuils {

    private static final String TUTORIAL_LINK = "https://github.com/Andre1299/andre1299-ConsoleLauncher/wiki";

    @SuppressWarnings("unchecked")
	public static String terminalCommand(String input, File dir, Resources res) throws Exception {

        if(input.equals("settings"))
            throw new Exception();

        final List<String> cmd = (List<String>) CommandTuils.getArg(null, input, CommandAbstraction.TEXTLIST).arg;

        boolean su = CommandTuils.isSuCommand(input);

        if(su) {
            if(!cmd.get(0).equals("su"))
                cmd.add(0, "su");
            if(!cmd.get(1).equals("-c"))
                cmd.add(1, "-c");
        }

        final Process process = new ProcessBuilder()
                .directory(dir)
                .command(cmd)
                .start();

        process.waitFor();

        if(process.exitValue() == 127)
            throw new IOException();

        InputStream i = process.getInputStream();
        InputStream e = process.getErrorStream();

        BufferedReader ir = new BufferedReader(new InputStreamReader(i));
        BufferedReader er = new BufferedReader(new InputStreamReader(e));

        String s = null, output = "";

//        error
        for(int count = 0; count == 0 || s != null; count++) {
            s = er.readLine();
            if(s != null) {
                if(count == 0)
                    output = output.concat(res.getString(R.string.error_label) + "\n");
                output = output.concat(s + "\n");
            }
        }

//       output
        for(int count = 0; count == 0 || s != null; count++) {
            s = ir.readLine();
            if(s != null)
                output = output.concat(s + "\n");
        }

        process.destroy();

        return output;
	}

    public static void showTutorial(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TUTORIAL_LINK));
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void openSettingsPage(Activity c) {
        openSettingsPage(c, null);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void openSettingsPage(Activity c, String toast) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", c.getPackageName(), null);
        intent.setData(uri);
        c.startActivity(intent);
        Toast.makeText(c, toast, Toast.LENGTH_LONG).show();
    }

	public static void requestAdmin(Activity a, ComponentName component, String label) {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);  
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);  
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, label);  
		a.startActivityForResult(intent, 0);  
	}
	
	public static void revokeAdmin(DevicePolicyManager manager, ComponentName who) {
		manager.removeActiveAdmin(who);
	}
	
	public static String ramDetails(ActivityManager mgr, MemoryInfo info) {
		mgr.getMemoryInfo(info);
		long availableMegs = info.availMem / 1048576L;
		
		return availableMegs + " MB";
	}
	
	public static List<String> getClassesOfPackage(String packageName, Context c) 
			throws IOException {
		List<String> classes = new ArrayList<>();
		String packageCodePath = c.getPackageCodePath();
		DexFile df = new DexFile(packageCodePath);
		for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
            String className = iter.nextElement();
            if (className.contains(packageName))
				classes.add(className.substring(className.lastIndexOf(".") + 1, className.length()));
		}

	    return classes;
	}
	
	@SuppressWarnings("unchecked")
	public static CommandAbstraction getCommandInstance(String cmdName) throws Exception {
		Class<CommandAbstraction> clazz = (Class<CommandAbstraction>) Class.forName(cmdName);
		Constructor<?> ctor = clazz.getConstructor();
		return (CommandAbstraction) ctor.newInstance();
	}
	
    public static int findPrefix(List<String> list, String prefix) {
        for (int count = 0; count < list.size(); count++)
            if(list.get(count).startsWith(prefix))
                return count;
        return -1;
    }

    public static int count(String string, String toCount) {
        return string.length() - string.replaceAll(toCount, "").length();
    }
	
	public static boolean verifyRoot() {
		Process p;   
		try {   
		   p = Runtime.getRuntime().exec("su");   
		     
		   DataOutputStream os = new DataOutputStream(p.getOutputStream());   
		   os.writeBytes("echo \"root?\" >/system/sd/temporary.txt\n");  
		     
		   os.writeBytes("exit\n");   
		   os.flush();   
		   try {   
		       p.waitFor();
               return p.exitValue() != 255;
		   } catch (InterruptedException e) {   
			   return false;
		   }   
		} catch (IOException e) {   
			return false;
		}
	}

    public static void insertHeaders(List<String> s) {
        char current = 0;
        for(int count = 0; count < s.size(); count++) {
            char c = 0;

            String st = s.get(count);
            for(int count2 = 0; count2 < st.length(); count2++) {
                c = st.charAt(count2);
                if(c != ' ')
                    break;
            }

            if(current != c) {
                s.add(count, Character.toString(c));
                current = c;
            }
        }
    }

    public static void addPrefix(List<String> list, String prefix) {
        for(int count = 0; count < list.size(); count++)
            list.set(count, prefix.concat(list.get(count)));
    }

    public static String toPlanString(String[] strings, String separator) {
        String output = "";
        for(int count = 0; count < strings.length; count++)  {
            output = output.concat(strings[count]);
            if(count < strings.length - 1)
                output = output.concat(separator);
        }
        return output;
    }

    public static String toPlanString(String[] strings) {
        return Tuils.toPlanString(strings, "\n");
    }
    
    public static String toPlanString(List<String> strings, String separator) {
        String[] object = new String[strings.size()];
        return Tuils.toPlanString(strings.toArray(object), separator);
    }
    
    public static String toPlanString(List<String> strings) {
    	return Tuils.toPlanString(strings, "\n");
    }

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

    public static boolean isAlpha(String s) {
        char[] chars = s.toCharArray();

        for (char c : chars)
            if(!Character.isLetter(c))
                return false;

        return true;
    }

    public static boolean isNumber(String s) {
        char[] chars = s.toCharArray();

        for (char c : chars)
            if(Character.isLetter(c))
                return false;

        return true;
    }

    public static String trimSpaces(String s) {
        while(s.startsWith(" "))
            s = s.substring(1);
        while(s.endsWith(" "))
            s = s.substring(0, s.length() - 1);
        return s;
    }

    public static String getSDK() {
        return "android-sdk " + Build.VERSION.SDK_INT;
    }

    public static String getUsername(Context context) {
        Pattern email = Patterns.EMAIL_ADDRESS;
        Account[]  accs = AccountManager.get(context).getAccounts();
        for(Account a : accs)
            if(email.matcher(a.name).matches())
                return a.name;
        return null;
    }

	public static Intent openFile(File url) {
		Uri uri = Uri.fromFile(url);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
			// Word document
			intent.setDataAndType(uri, "application/msword");
		} else if (url.toString().contains(".apk")) {
			// apk
			intent.setDataAndType(uri,
					"application/vnd.android.package-archive");
		} else if (url.toString().contains(".pdf")) {
			// PDF file
			intent.setDataAndType(uri, "application/pdf");
		} else if (url.toString().contains(".ppt")
				|| url.toString().contains(".pptx")) {
			// Powerpoint file
			intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		} else if (url.toString().contains(".xls")
				|| url.toString().contains(".xlsx")) {
			// Excel file
			intent.setDataAndType(uri, "application/vnd.ms-excel");
		} else if (url.toString().contains(".zip")
				|| url.toString().contains(".rar")) {
			// ZIP Files
			intent.setDataAndType(uri, "application/zip");
		} else if (url.toString().contains(".rtf")) {
			// RTF file
			intent.setDataAndType(uri, "application/rtf");
		} else if (url.toString().contains(".wav")
				|| url.toString().contains(".mp3")) {
			// WAV audio file
			intent.setDataAndType(uri, "audio/x-wav");
		} else if (url.toString().contains(".gif")) {
			// GIF file
			intent.setDataAndType(uri, "image/gif");
		} else if (url.toString().contains(".jpg")
				|| url.toString().contains(".jpeg")
				|| url.toString().contains(".png")) {
			// JPG file
			intent.setDataAndType(uri, "image/jpeg");
		} else if (url.toString().contains(".txt")) {
			// Text file
			intent.setDataAndType(uri, "text/plain");
		} else if (url.toString().contains(".3gp")
				|| url.toString().contains(".mpg")
				|| url.toString().contains(".mpeg")
				|| url.toString().contains(".mpe")
				|| url.toString().contains(".mp4")
				|| url.toString().contains(".avi")) {
			// Video files
			intent.setDataAndType(uri, "video/*");
		} else {
			intent.setDataAndType(uri, "*/*");
		}

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
	
	public static String getInternalDirectoryPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

}
