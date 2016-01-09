package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import ohi.andre.comparestring.Compare;

public class ContactManager {

    private final int MIN_RATE = 4;

	private Map<String, String> contacts;
	
	private Random random;
	
	public ContactManager(Random random) {
		this.random = random;
		contacts = new TreeMap<>();
	}
	
	public void init(Context c) throws NullPointerException {
		Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null,null);
		while (phones.moveToNext()) {
			String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			
			contacts.put(name, phoneNumber);
		}
		phones.close();
	}
	
	public Set<String> names() {
		return contacts.keySet();
	}
	
	public ArrayList<String> list() {
		ArrayList<String> values = new ArrayList<>();
		
		Set<Entry<String, String>> set = contacts.entrySet();
		for(Entry<String, String> entry : set) 
			values.add(entry.getKey() + "\t:\t" + entry.getValue());
			
		return values;
	}
	
    public String findNumber(String name) {
    	String mostSuitable = Compare.compare(contacts.keySet(), name, MIN_RATE, random);
    	return mostSuitable == null ? null : contacts.get(mostSuitable);
    }
}
