/*
 * Copyright (C) 2014 Lee Chee Meng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.timelesssky.systemtrigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class SystemReceiver extends BroadcastReceiver {

    private static final String TAG = SystemReceiver.class.getCanonicalName();

    private static final String EXTRA_SHUTDOWN_USERSPACE_ONLY =
        (Build.VERSION.SDK_INT >= 19)? Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY
                : "android.intent.extra.SHUTDOWN_USERSPACE_ONLY";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            runCommand(App.getCmdStartup());
            String appStartup = App.getStartupApp();
            if (!appStartup.isEmpty())
                App.launchApp(context.getApplicationContext(), appStartup);
        }
        else if (action.equals(Intent.ACTION_SHUTDOWN)) {
            // in API 19
            boolean isUserspace = intent.getBooleanExtra(
                    EXTRA_SHUTDOWN_USERSPACE_ONLY, false);
            if (isUserspace)
                return;
            runCommand(App.getCmdShutdown());
        }
    }

    private void runCommand(String cmd) {
        try {
            if ((cmd.isEmpty()) || (!App.verifyCommand(cmd))) {
                Log.i(TAG, "invalid command: " + cmd);
                return;
            }
            Log.i(TAG, "exec command: " + cmd);
            String[] cmds = cmd.split(" ");
            Runtime.getRuntime().exec(cmds);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}

