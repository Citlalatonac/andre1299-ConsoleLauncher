package ohi.andre.consolelauncher.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 18/12/15.
 */
public class ChangelogManager {

    private final String CHANGELOG_FILENAME = "changelog.txt";
    private final String CHANGELOG_LABEL = "Changelog:";
    private final String CHANGELOG_VERSION_VALUE = "3.2";
    private final String[] CHANGELOG_VALUE = {
            CHANGELOG_LABEL + " " + CHANGELOG_VERSION_VALUE,
            " ",
            " - bugfix",
            " - performance improvements",
            " - tutorial updated"
    };

    private File changelogFile;

    public ChangelogManager(File folder) {
        changelogFile = new File(folder, CHANGELOG_FILENAME);
    }

    public void write() throws IOException {
        if(changelogFile.exists())
            changelogFile.delete();
        changelogFile.createNewFile();

        FileOutputStream stream = new FileOutputStream(changelogFile);

        stream.write(Tuils.toPlanString(CHANGELOG_VALUE, "\n").getBytes());

        stream.flush();
        stream.close();
    }

    public boolean needUpdate() throws Exception {
        if (!changelogFile.exists())
            return true;

        FileInputStream fis = new FileInputStream(changelogFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

//        changelog line
        String line = reader.readLine();
        int equalsIndex = line.indexOf("=") + 1;

        fis.close();
        reader.close();

        return equalsIndex != -1 && !line.substring(equalsIndex).equals(CHANGELOG_VERSION_VALUE);
    }
}
