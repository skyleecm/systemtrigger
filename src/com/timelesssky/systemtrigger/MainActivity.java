/*
 * Copyright (C) 2014 Lee Chee Meng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.timelesssky.systemtrigger;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import eu.chainfire.libsuperuser.Shell;


public class MainActivity extends Activity
{
    private EditText editStartupApp;
    private EditText editCmdStartup;
    private EditText editCmdShutdown;
    private TextView txtMsg;
    private Button btnCheckCmd;
    private Button btnSave;
    private Button btnLaunch;
    private Button btnReboot;
    private Button btnRebootRecovery;
    private boolean suAvailable = false;
    public static final String cmd_prop_shutdown = "setprop sys.shutdown.requested 1";
    public static final String cmd_bc_shutdown = "am broadcast -a " + Intent.ACTION_SHUTDOWN;
    public static final String cmd_wait_shutdown = "sleep ";
    public static final String cmd_reboot = "/system/bin/reboot";
    public static final String cmd_reboot_recovery = "/system/bin/reboot recovery";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        editStartupApp = (EditText) findViewById(R.id.textStartupAppPackage);
        editCmdStartup = (EditText) findViewById(R.id.textStartupCmd);
        editCmdShutdown = (EditText) findViewById(R.id.textShutdownCmd);
        btnLaunch = (Button) findViewById(R.id.btnLaunch);
        btnCheckCmd = (Button) findViewById(R.id.btnCheckCmd);
        btnSave = (Button) findViewById(R.id.btnSave);
        txtMsg = (TextView) findViewById(R.id.msgCheck);

        load();

        btnCheckCmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCommands();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCommands();
            }
        });
        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.launchApp(MainActivity.this, editStartupApp.getText().toString());
            }
        });
        btnReboot = (Button) findViewById(R.id.btnReboot);
        btnRebootRecovery = (Button) findViewById(R.id.btnRebootRecovery);
        // suAvailable
        (new SuCheck()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.menuAbout:
                showAbout();
                return true;
            case R.id.menuSettings:
                showPrefs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // load from default preference
    private void load() {
        editCmdStartup.setText(App.getCmdStartup());
        editCmdShutdown.setText(App.getCmdShutdown());
        editStartupApp.setText(App.getStartupApp());
    }

    // save the cmds to default preference
    public boolean saveCommands() {
        String cmdStartup = editCmdStartup.getText().toString();
        String cmdShutdown = editCmdShutdown.getText().toString();
        String appStartup = editStartupApp.getText().toString();
        App.saveCmdStartup(cmdStartup);
        App.saveCmdShutdown(cmdShutdown);
        App.saveStartupApp(appStartup);
        txtMsg.setText(getString(R.string.msg_cmd_saved));
        return true;
    }

        // verify the cmds exists
    public boolean verifyCommands() {
        String cmdStartup = editCmdStartup.getText().toString();
        String cmdShutdown = editCmdShutdown.getText().toString();
        boolean bStartup = !cmdStartup.isEmpty();
        boolean bShutdown = !cmdShutdown.isEmpty();
        if (bStartup || bShutdown) {
            bStartup = App.verifyCommand(cmdStartup);
            bShutdown = App.verifyCommand(cmdShutdown);
            String msg = "";
            if (!bStartup)
                msg = getString(R.string.msg_cmd_invalid, cmdStartup) + "\n";
            if (!bShutdown)
                msg = msg + getString(R.string.msg_cmd_invalid, cmdShutdown) + "\n";
            if (msg.isEmpty())
                msg = getString(R.string.msg_cmd_isok);
            txtMsg.setText(msg);
            return bStartup && bShutdown;
        }
        else {
            txtMsg.setText(getString(R.string.msg_no_cmd));
            return true;
        }
    }

    void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getString(R.string.app_name) + " - v "
                + getString(R.string.version_name);
        builder.setTitle(title);
        String info = getString(R.string.about_license).replace("\n ", "\n");
        builder.setMessage(info);
        final AlertDialog dialog = builder.create();
        FragmentManager manager = getFragmentManager();
        DialogFragment frag = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return dialog;
            }
        };
        frag.setRetainInstance(true); // fix rotation
        frag.show(manager, "About");
    }

    void showPrefs() {
        Intent intent = new Intent(this, PrefsActivity.class);
        startActivity(intent);
    }

    private void setSuCommand(final Button btn, final String cmd) {
        btn.setEnabled(true);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new App.SuCmd()).execute(cmd_prop_shutdown,
                        cmd_bc_shutdown, 
                        cmd_wait_shutdown + App.getRebootWait(), 
                        cmd);
            }
        });
    }

    private class SuCheck extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... p) {
            suAvailable = Shell.SU.available();
            return suAvailable;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setSuCommand(btnReboot, cmd_reboot);
                setSuCommand(btnRebootRecovery, cmd_reboot_recovery);
            }
            else { 
                btnReboot.setEnabled(false);
                btnRebootRecovery.setEnabled(false);
            }
        }
    }

}
