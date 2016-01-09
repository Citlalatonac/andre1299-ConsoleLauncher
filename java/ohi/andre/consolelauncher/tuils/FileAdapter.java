package ohi.andre.consolelauncher.tuils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.SkinManager;

public class FileAdapter extends BaseAdapter {
	
	private ArrayList<File> files;
	private Context mContext;
	private SkinManager mSkin;
	
	private String superLabel;
	
	public FileAdapter(Context context, SkinManager skin) {
		files = new ArrayList<>();
		
		mContext = context;
		mSkin = skin;
		
		superLabel = "..";
	}
	
	public void clear() {
		files.clear();
	}
	
	public void add(File f) {
		files.add(f);
	}
	
	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addSuperLabel(File parent) {
		if(parent == null)
			return;
		files.add(parent);
	}

    @SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        File f = (File) getItem(position);

        if(convertView == null)
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.file_view, null);

        if(mSkin.needInflate(convertView, f.isDirectory()))
        	mSkin.setupFileViews((TextView) convertView, f.isDirectory());
        
        String name;
        if(files.indexOf(f) == files.size() - 1)
        	name = superLabel;
        else
        	name = f.getName();
		
		((TextView) convertView).setText(name);
		
		return convertView;
	}

}
