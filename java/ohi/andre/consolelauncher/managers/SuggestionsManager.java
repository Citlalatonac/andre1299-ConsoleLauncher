package ohi.andre.consolelauncher.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ohi.andre.comparestring.Compare;
import ohi.andre.consolelauncher.commands.Command;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.CommandTuils;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.tuils.AppInfo;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 25/12/15.
 */
public class SuggestionsManager {

    private final int MIN_COMMAND_RATE = 2;
    private final int MIN_COMMAND_PRIORITY = 5;
    private final int MIN_APPS_RATE = 4;
    private final int MIN_CONTACTS_RATE = 3;
    private final int MIN_FILE_RATE = 3;

    private ExecInfo info;

    public SuggestionsManager(ExecInfo info) {
        this.info = info;
    }

//    files in cd
//    apps
//    alias
//    contacts/songs in certain commands
//    commands in help
//    params
    public String[] getSuggestions(String before, String lastWord) {
        List<String> suggestionList = new ArrayList<>();

        before = Tuils.trimSpaces(before);
        lastWord = Tuils.trimSpaces(lastWord);

//        if nothing was typed after before
        if(lastWord.length() == 0) {
//            if there is no before
            if(before.length() == 0) {
                suggestAlias(suggestionList);
                suggestCommand(suggestionList);
                return toSuggestionArray(suggestionList);
            }
//            if there is before
            else {
//                check if this is a command
                Command cmd = null;
                try {
                    cmd = CommandTuils.parse(before, info);
                } catch (Exception e) {}

                if(cmd != null) {
                    if(cmd.nArgs == cmd.cmd.maxArgs())
                        return new String[0];
                    if(cmd.nArgs == 0)
                        suggestParams(suggestionList, cmd.cmd);
                    suggestArgs(cmd.nextArg(), suggestionList);
                } else {
//                    something typed
//                    but contains spaces ==> not command
                    suggestApp(suggestionList, before);
                }
            }
        }
//        last word is not 0, and doesnt contains spaces
        else {
            if(before.length() > 0) {
                Command cmd = null;
                try {
                    cmd = CommandTuils.parse(before, info);
                } catch (Exception e) {}

                if (cmd != null) {
//                params?
//                if this is the first arg after command
                    if (cmd.nArgs == 0)
                        suggestParams(suggestionList, cmd.cmd);

                    if (cmd.nArgs != cmd.cmd.maxArgs())
                        suggestArgs(cmd.nextArg(), suggestionList, lastWord);
                } else {
//                    not a command
                    suggestApp(suggestionList, before.concat(lastWord));
                }
            } else {
                Command cmd = null;
                try {
                    cmd = CommandTuils.parse(before, info);
                } catch (Exception e) {}

                if(cmd == null) {
                    suggestCommand(suggestionList, lastWord);
                    suggestApp(suggestionList, lastWord);
                    suggestAlias(suggestionList);
                } else {
                    suggestParams(suggestionList, cmd.cmd);
                }
            }
        }

        return toSuggestionArray(suggestionList);
    }

    private void suggestAlias(List<String> suggestions) {
        Set<String> alias = info.aliasManager.getAliass();
        suggestions.addAll(alias);
    }

    private void suggestParams(List<String> suggestions, CommandAbstraction cmd) {
        String[] params = cmd.parameters();
        if(params == null)
            return;
        suggestions.addAll(Arrays.asList(cmd.parameters()));
    }

    private void suggestArgs(int type, List<String> suggestions, String prev) {
        switch (type) {
            case CommandAbstraction.FILE:case CommandAbstraction.FILE_LIST:
                suggestFile(suggestions, prev);
                break;
            case CommandAbstraction.PACKAGE:
                suggestApp(suggestions, prev);
                break;
            case CommandAbstraction.COMMAND:
                suggestCommand(suggestions, prev);
            case CommandAbstraction.CONTACTNUMBER:
                suggestContact(suggestions, prev);
        }
    }

    private void suggestArgs(int type, List<String> suggestions) {
        suggestArgs(type, suggestions, null);
    }

    private void suggestFile(List<String> suggestions, String prev) {
        if(prev == null)
            return;

        if(!prev.contains("/")) {
            for(File file : info.currentDirectory.listFiles())
                if(Compare.compare(prev, file.getName()) >= MIN_FILE_RATE)
                    suggestions.add(file.getName());
        }
        else if(prev.length() > 0) {
            FileManager.DirInfo dirInfo = FileManager.cd(info.currentDirectory, prev);
            if(dirInfo.file.isDirectory()) {
                String name = prev.substring(prev.indexOf("/") + 1);
                for (File file : dirInfo.file.listFiles())
                    if (Compare.compare(name, file.getName()) >= MIN_FILE_RATE)
                        suggestions.add(file.getName());
            }
        }
    }

    private void suggestContact(List<String> suggestions, String prev) {
        for(String s : info.contacts.names()) {
            if (prev != null) {
                if (Compare.compare(prev, s) >= MIN_CONTACTS_RATE)
                    suggestions.add(s);
            } else
                suggestions.add(s);
        }
    }

    private void suggestCommand(List<String> suggestions, String prev) {
        if(prev == null) {
            suggestCommand(suggestions);
            return;
        }

        for(String s : info.active.getCommands())
            if(Compare.compare(prev, s) >= MIN_COMMAND_RATE)
                suggestions.add(s);
    }

    private void suggestCommand(List<String> suggestions) {
        for(String s : info.active.getCommands()) {
            CommandAbstraction cmd = null;
            try {
                cmd = info.active.getCommandByName(s);
            } catch (Exception e) {}

            if(cmd.priority() >= MIN_COMMAND_PRIORITY)
                suggestions.add(s);
        }
    }

    private void suggestApp(List<String> suggestions, String prev) {
        if(prev == null)
            return;

        for(AppInfo a : info.appsManager.getApps())
            if(Compare.compare(prev, a.publicLabel) >= MIN_APPS_RATE)
                suggestions.add(a.publicLabel);
    }

    private String[] toSuggestionArray(List<String> list) {
        String[] strings = new String[list.size()];
        return list.toArray(strings);
    }
}
