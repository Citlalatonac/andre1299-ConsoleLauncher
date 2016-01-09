package ohi.andre.consolelauncher.managers;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SkinManager {

	public static final int SYSTEM_WALLPAPER = -1;

	private final int SUGGESTION_PADDING_VERTICAL = 15;
	private final int SUGGESTION_PADDING_HORIZONTAL = 15;
	public static final int SUGGESTION_MARGIN = 20;

	private Typeface typeface;

    private int fontSize;
    
	private int device;
	private int input;
	private int output;
	private int ram;
	private int dir;
	private int files;
    private int folders;
	private int bg;
	private int suggestionColor;
	private int suggestionBg;

//	default
	public static final int deviceDefault = 0xffff9800;
	public static final int inputDefault = 0xff00ff00;
	public static final int outputDefault = 0xffffffff;
	public static final int ramDefault = 0xfff44336;
	public static final int dirDefault = 0xff607d8b;
	public static final int filesDefault = 0xffffffff;
	public static final int foldersDefault = 0xffffffff;
	public static final int bgDefault = 0xff004d40;
	public static final int suggestionColorDefault = 0xff000000;
	public static final int suggestionBgDefault = 0xffffffff;

	private static final int deviceScale = 3;
	private static final int inputScale = 2;
	private static final int outputScale = 2;
	private static final int ramScale = 3;
	private static final int dirScale = 3;
	private static final int filesScale = -1;
	private static final int submitScale = inputScale;
	private static final int suggestionScale = 0;
	
	public static final int defaultSize = 15;
	
	public SkinManager(PreferencesManager prefs, Typeface lucidaConsole, Resources resources, boolean isFileType,
					   boolean showSuggestion) {

        boolean systemFont = Boolean.parseBoolean(prefs.getValue(PreferencesManager.USE_SYSTEMFONT));
		typeface = systemFont ? Typeface.DEFAULT : lucidaConsole;

		try {
			fontSize = Integer.parseInt(prefs.getValue(PreferencesManager.FONTSIZE));
		} catch(Exception e) {
			fontSize = SkinManager.defaultSize;
		}
		
		try {
			bg = Color.parseColor(prefs.getValue(PreferencesManager.BG));
		} catch(Exception e) {
			bg = bgDefault;
		}

		try {
			device = Color.parseColor(prefs.getValue(PreferencesManager.DEVICE));
		} catch(Exception e) {
			device = deviceDefault;
		}
		
		try {
			input = Color.parseColor(prefs.getValue(PreferencesManager.INPUT));
		} catch(Exception e) {
			input = inputDefault;
		}
		
		try {
			output = Color.parseColor(prefs.getValue(PreferencesManager.OUTPUT));
		} catch(Exception e) {
			output = outputDefault;
		}

        try {
            ram = Color.parseColor(prefs.getValue(PreferencesManager.RAM));
        } catch(Exception e) {
            ram = ramDefault;
        }

		if(showSuggestion) {
			try {
				suggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.SUGGESTION_COLOR));
			} catch(Exception e) {
				suggestionColor = suggestionColorDefault;
			}

			try {
				suggestionBg = Color.parseColor(prefs.getValue(PreferencesManager.SUGGESTION_BG));
			} catch(Exception e) {
				suggestionBg = suggestionBgDefault;
			}
		}

		if(!isFileType)
			return;
		
		try {
			dir = Color.parseColor(prefs.getValue(PreferencesManager.DIRECTORY));
		} catch(Exception e) {
			dir = dirDefault;
		}
 
		try {
			files = Color.parseColor(prefs.getValue(PreferencesManager.FILES));
		} catch(Exception e) {
			files = filesDefault;
		}
		
		try {
			folders = Color.parseColor(prefs.getValue(PreferencesManager.FOLDERS));
		} catch(Exception e) {
			folders = foldersDefault;
		}
	}
	
	public void setupBg(View bgView) {
		if(bg != SkinManager.SYSTEM_WALLPAPER)
            bgView.setBackgroundColor(bg);
	}

	public void setupDeviceInfo(TextView deviceView) {
		deviceView.setTextColor(device);
		deviceView.setTextSize(fontSize + SkinManager.deviceScale);

		deviceView.setTypeface(typeface);
	}
	
	public void setupInput(TextView inputView) {
		inputView.setTextColor(input);
		inputView.setTextSize(fontSize + SkinManager.inputScale);
		
		inputView.setTypeface(typeface);
	}

	public void setupSubmit(Button submit) {
		submit.setBackgroundColor(bg);
		submit.setTextColor(input);
		submit.setTextSize(fontSize + SkinManager.submitScale);
	}

	public void setupOutput(TextView outputView) {
        outputView.setTextColor(output);
		outputView.setTextSize(fontSize + SkinManager.outputScale);
		
		outputView.setTypeface(typeface);
	}
	
	public void setupRam(TextView ramView) {
		ramView.setTextColor(ram);
		ramView.setTextSize(fontSize + SkinManager.ramScale);
		
		ramView.setTypeface(typeface);
	}
	
	public void setupDir(TextView dirView) {
		dirView.setTextColor(dir);
		dirView.setTextSize(fontSize + SkinManager.dirScale); 
		
		dirView.setTypeface(typeface);
	}
	
	public void setupFileViews(TextView tv, boolean directory) {
		if(directory)
            tv.setTextColor(folders);
        else
            tv.setTextColor(files);

		tv.setTypeface(typeface);
		tv.setTextSize(fontSize + SkinManager.filesScale);
	}

	public void setupSuggestion(TextView textView) {
		textView.setTypeface(typeface);
		textView.setTextSize(fontSize + suggestionScale);
		textView.setTextColor(suggestionColor);
		textView.setPadding(SUGGESTION_PADDING_HORIZONTAL, SUGGESTION_PADDING_VERTICAL, SUGGESTION_PADDING_HORIZONTAL,
				SUGGESTION_PADDING_VERTICAL);
		textView.setBackgroundColor(suggestionBg);
	}

    public boolean needInflate(View v, boolean directory) {
        int color = ((TextView) v).getCurrentTextColor();
        if(directory) {
            return color != folders;
        } else {
            return color != files;
        }
    }
	
}
