/*
 * Copyright (C) 2014 Lee Chee Meng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.timelesssky.systemtrigger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

public class App extends Application {

    private static final String TAG = App.class.getCanonicalName();

    private static SharedPreferences appPref;

    @Override
    public void onCreate() {
        super.onCreate();
        appPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static String getCmdStartup() {
        return appPref.getString("cmdStartup", "");
    }

    public static String getCmdShutdown() {
        return appPref.getString("cmdShutdown", "");
    }

    public static String getStartupApp() {
        return appPref.getString("appStartup", "");
    }

    static void saveCmdStartup(String cmd) {
        appPref.edit().putString("cmdStartup", cmd).apply();
    }

    static void saveCmdShutdown(String cmd) {
        appPref.edit().putString("cmdShutdown", cmd).apply();
    }

    static void saveStartupApp(String app) {
        appPref.edit().putString("appStartup", app).apply();
    }

    // launch an app
    public static void launchApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null)
                context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    // verify the cmd exists
    public static boolean verifyCommand(String cmd) {
        if (cmd.isEmpty())
            return true;
        String[] cmds = cmd.split(" ", 2);
        File file = new File(cmds[0]);
        return file.isFile() && file.canExecute();
    }
}
