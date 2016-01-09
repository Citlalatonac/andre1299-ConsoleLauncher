package ohi.andre.consolelauncher.commands;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ohi.andre.comparestring.Compare;
import ohi.andre.consolelauncher.tuils.Tuils;

public class CommandGroup {
	
	private String packageName;
	
	private List<String> commands;
	
	public CommandGroup(Context c, String packageName) {
		this.packageName = packageName;
		
		try {
			this.commands = Tuils.getClassesOfPackage(packageName, c);
		} catch (IOException e) {}
		
		Collections.sort(commands);
	}
	
	public String printCommands() {
        List<String> toPrint = new ArrayList<>(commands);

        Collections.sort(toPrint, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return Compare.alphabeticCompare(lhs, rhs);
            }
        });

        Tuils.addPrefix(toPrint, "  ");
        Tuils.insertHeaders(toPrint);
		
		return Tuils.toPlanString(toPrint);
	}
	
	public CommandAbstraction getCommandByName(String name) throws Exception {
		for(int count = 0; count < commands.size(); count++) {
			String cmdName = commands.get(count);
			if(!cmdName.equals(name))
				continue;
			
			String fullCmdName = packageName + "." + cmdName;
			return Tuils.getCommandInstance(fullCmdName);
		}
		
		return null;
	}

	public List<String> getCommands() {
		return commands;
	}
	
}
