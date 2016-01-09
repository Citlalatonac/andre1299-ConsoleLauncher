package ohi.andre.consolelauncher.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.Command;
import ohi.andre.consolelauncher.commands.CommandGroup;
import ohi.andre.consolelauncher.commands.CommandTuils;
import ohi.andre.consolelauncher.commands.ExecInfo;
import ohi.andre.consolelauncher.managers.AliasManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.MusicManager;
import ohi.andre.consolelauncher.managers.PreferencesManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Clearer;
import ohi.andre.consolelauncher.tuils.interfaces.CmdTrigger;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;

/**
 * Created by andre on 03/11/15.
 */
public abstract class MainManager {

    private final String COMMANDS_PKG = "ohi.andre.consolelauncher.commands.raw";
    private final int LAST_COMMANDS_SIZE = 20;
    private AppsManager appsMgr;
    private AliasManager aliasManager;
    private CmdTrigger[] triggers;
    private ExecInfo info;
    private Context mContext;
    private Inputable in;
    private Outputable out;
    private ArrayList<String> lastCommands;
    private int lastCommandIndex;

    protected MainManager(LauncherActivity c, Inputable i, Outputable o, PreferencesManager prefsMgr,
                          DevicePolicyManager devicePolicyManager, ComponentName componentName, Clearer clearer) {
        mContext = c;

        in = i;
        out = o;

        lastCommands = new ArrayList<>(LAST_COMMANDS_SIZE);

        Random random = new Random();

        if(info != null) {
            info.dispose();
            if(info.player != null)
                info.player.dispose();
        }

        CommandGroup group = new CommandGroup(mContext, COMMANDS_PKG);
        initLauncherMode();

        ContactManager cont = new ContactManager(random);
        try {
            cont.init(mContext);
        } catch (NullPointerException e) {}

        MusicManager music = new MusicManager();
        music.init(prefsMgr);

        appsMgr = new AppsManager(c, random);
        aliasManager = new AliasManager(prefsMgr);

        info = new ExecInfo(mContext, group, aliasManager, appsMgr, music, cont, devicePolicyManager, componentName,
                c, clearer);
    }

//    command manager
    public void onCommand(String input) {
        if(input.length() == 0)
            return;

        if(lastCommands.size() == LAST_COMMANDS_SIZE)
            lastCommands.remove(0);
        lastCommands.add(input);
        lastCommandIndex = lastCommands.size() - 1;

        int r;
        for (CmdTrigger trigger : triggers) {
            try {
                r = trigger.trigger(input);
            } catch (Exception e) {
                out.onOutput(Tuils.getStackTrace(e), false);
                return;
            }
            if (r == 0)
                return;
        }

        out.onOutput(mContext.getString(R.string.output_commandnotfound), true);
    }

//    init launcher
    private void initLauncherMode() {
        triggers = new CmdTrigger[3];
        triggers[0] = new AliasTrigger();
        triggers[1] = new CommandTrigger();
        triggers[2] = new AppsTrigger();
    }

//    back managers
    public void onBackPressed() {
        String s;
        if(lastCommands.size() > 0 && lastCommandIndex < lastCommands.size() && lastCommandIndex >= 0)
            s = lastCommands.get(lastCommandIndex--);
        else
            s = "";

        in.in(s);
    }

    public void onLongBack() {
        in.in("");
    }

//    dispose
    public void dispose() {
        info.dispose();
    }

    public ExecInfo getInfo() {
        return info;
    }

    public File getCurrentDirectory() {
        return info.currentDirectory;
    }

//    apps
    public void onAddApp(String packageName) {
        appsMgr.add(packageName);
    }

    public void onRemApp(String packageName) {
        appsMgr.remove(packageName);
    }

//    music
    public void onHeadsetUnplugged() {
    info.player.onHeadsetUnplugged();
}

//    triggers
    private class CommandTrigger implements CmdTrigger {

        @Override
        public int trigger(String input) throws Exception {
            String output;
            boolean rt;

            input = Tuils.trimSpaces(input);

            Command command = CommandTuils.parse(input, info);
            if(command != null) {
                rt = command.useRealTimeTyping();
                output = command.exec(info);

                if(info.dirUpdater != null)
                    info.dirUpdater.update();
            } else if(CommandTuils.isSuRequest(input)) {
                rt = true;
                if(Tuils.verifyRoot())
                    output = mContext.getString(R.string.su);
                else
                    output = mContext.getString(R.string.nosu);
            } else {
                try {
                    output = Tuils.terminalCommand(input, info.currentDirectory, info.res);
                    rt = false;

                    if(info.dirUpdater != null)
                        info.dirUpdater.update();
                } catch(Exception e) {
                    return 1;
                }
            }
            out.onOutput(output, rt);

            return 0;
        }

    }

    private class AppsTrigger implements CmdTrigger {
        @Override
        public int trigger(String input) {
            Intent app = appsMgr.getIntent(input);
            if(app == null)
                return 1;

            out.onOutput(
                    mContext.getString(R.string.running) + " " + app.getStringExtra(AppsManager.APP_LABEL),
                    false
            );

            mContext.startActivity(app);

            return 0;
        }

    }

    private class AliasTrigger implements CmdTrigger {

        @Override
        public int trigger(String input) {
            String alias = aliasManager.getAlias(input);
            if(alias == null)
                return 1;

            onCommand(alias);

            return 0;
        }

    }
}
