package ohi.andre.consolelauncher.commands;

public interface CommandAbstraction {
	
//	undefinied n of arguments
	int UNDEFINIED = -1;

//	arg type
	int PLAIN_TEXT = 10;
	int FILE = 11;
	int PACKAGE = 12;
	int CONTACTNUMBER = 13;
	int TEXTLIST = 14;
	int SONG = 15;
	int FILE_LIST = 16;
	int COMMAND = 17;

	String exec(ExecInfo info) throws Exception;

	boolean useRealTime();
	
	int minArgs();
	int maxArgs();
	int[] argType();
	int priority();

	int helpRes();
	int notFoundRes();
	String onNotArgEnough(ExecInfo info);

	String[] parameters();
}
