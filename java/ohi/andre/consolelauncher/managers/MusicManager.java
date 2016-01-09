package ohi.andre.consolelauncher.managers;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ohi.andre.comparestring.Compare;
import ohi.andre.consolelauncher.tuils.interfaces.OnHeadsetStateChangedListener;

public class MusicManager implements OnCompletionListener, OnHeadsetStateChangedListener {

    private int MIN_RATE = 5;

	private List<String> songsPath;
	private File songFolder;
	private boolean randomActive;

	private MediaPlayer mp;
	private int currentSongIndex = 0; 
	
	public MusicManager() {
		this.mp = new MediaPlayer();
		this.mp.setOnCompletionListener(this);
	}
	
	public void init(PreferencesManager prefsMgr) {
		randomActive = Boolean.parseBoolean(prefsMgr.getValue(PreferencesManager.PLAY_RANDOM));
		songFolder = new File(prefsMgr.getValue(PreferencesManager.SONGSFOLDER));
		
		init();
	}
	
	public void init() {
		try {
			fillSongs(songFolder, randomActive);
		} catch(Exception e) {}
	}

    public boolean initPlayer() {
        return prepareSong(currentSongIndex);
    }
    
    private String getSong(ArrayList<String> names, String s) {
    	String mostSuitable = Compare.compare(names, s, MIN_RATE);
        if(mostSuitable == null)
            return null;
        return mostSuitable;
    }
    
    public String getSong(String s) {
    	return getSong(getNames(), s);
    }

    public boolean jukebox(String song) {
    	ArrayList<String> names = getNames();
    	
    	int index = names.indexOf(song);
    	if(index == -1)
    		return false;
    	
        prepareSong(index);
        play();
        return true;
    }
    
    public boolean isPlaying() {
    	try {
    		return mp.isPlaying();
    	} catch(Exception e) {
    		return false;
    	}
    }

    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for(String song : songsPath)
            names.add(new File(song).getName());
        return names;
    }

	
	private void fillSongs(File f, boolean shuffle) {
		songsPath = getSongsInFolder(f);
		verifyRandom(shuffle);
	}

	private List<String> getSongsInFolder(File folder) {
		ArrayList<String> songs = new ArrayList<>();

		File[] files = folder.listFiles();
		for(File file : files) {
			if(file.isDirectory())
				songs.addAll(getSongsInFolder(file));
			else if(file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3"))
				songs.add(file.getAbsolutePath());
		}

		return songs;
	}
	
	private void verifyRandom(boolean random) {
		if(random)
			Collections.shuffle(songsPath);
		else
			Collections.sort(songsPath);
	}
	
	private boolean prepareSong(int songIndex) {
		if(songsPath == null || songsPath.size() == 0)
			return false;

        currentSongIndex = songIndex;
		
		try {
			mp.reset();
			mp.setDataSource(songsPath.get(songIndex));
			mp.prepare();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public MusicManager.Status play() throws IllegalStateException{
		if(mp != null) {
			boolean playing;
            if(mp.isPlaying()) {
				mp.pause();
				playing = false;
			}
			else {
				mp.start();
                playing = true;
			}
			return new Status(new File(songsPath.get(currentSongIndex)).getName(), playing);
		}
		return null;
	}

	public String next() throws IllegalStateException{
		if(currentSongIndex < songsPath.size() - 1)
			currentSongIndex += 1;
		else 
			currentSongIndex = 0;
		prepareSong(currentSongIndex);
		mp.start();
		return new File(songsPath.get(currentSongIndex)).getName();
	}

	public String prev() throws IllegalStateException{
		if(currentSongIndex > 0){
			currentSongIndex -= 1;
			prepareSong(currentSongIndex);
			mp.start();
            return new File(songsPath.get(currentSongIndex)).getName();
		}
		return null;
	}
	
	public String restart() throws IllegalStateException{
		prepareSong(currentSongIndex);
		mp.start();
        return new File(songsPath.get(currentSongIndex)).getName();
	}

	public void stop() throws IllegalStateException{
        mp.stop();
	}
	
	public String trackInfo() {
		int total = mp.getDuration() / 1000;
		int position = mp.getCurrentPosition() / 1000;
		File f = new File(songsPath.get(currentSongIndex));
		return f.getName() + 
				"\n" + (total / 60) + "." + (total % 60)  + " / " + (position / 60) + "." + (position % 60)  +
				" (" + (100 * position / total) + "%)";
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		next();
	}
	
	@Override
	public void onHeadsetUnplugged() {
		if(mp != null && mp.isPlaying())
			mp.pause();
	}
	
	public void dispose() {
		mp.release();
	}
	
	
	public static class Status {
		public String song;
		public boolean playing;
		
		public Status(String s, boolean b) {
			this.song = s;
			this.playing = b;
		}
	}
	
}
