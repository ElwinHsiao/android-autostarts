package com.elsdoerfer.android.autostarts;

import android.Manifest.permission;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;

/**
How we toggle a component's states. Beware: This is a long comment.

Android has setComponentEnabledState(), but to use it on other apps
requires the CHANGE_COMPONENT_ENABLED_STATE permission, which is
signature-protected. Rarely will we be signed with the system
certificate, but for what it's worth, if we do have the permission,
we'll use it and make the call directly.

Normally we don't, and that means we need to use root to make a su
call to the "pm" command. "pm" is included in Android and is a command
line interface to the PackageManager service. Called as root, the
PackageManager will skip the permissions check.

Ideally, here's where the story would end. Unfortunately, there seems
to be a bug *somewhere* (in Dalvik? In the Superuser Whitelist app?)
that causes Process.waitFor() to hang if we run any process that involves
the "app_process" executable while "USB Debugging" is disabled.
"app_process" is a tool included in Android that will spawn a Dalvik VM
and execute Java code (i.e., the actual pm Command is a wrapper script
around app_process, the actual command is implemented as a Java class).

Note that the guilty party indeed is "app_process", not the "pm" command
in particular. Other commands like "ime" exhibit the same behavior.

The fact that the problem only occurs when ADB is disabled makes this
particularly hard to debug.

I've tried all kinds of things to find a solution, running "app_process"
directly, replacing Java's "Process" with a custom native library
calling "system" to run the command, but to no avail. Further options
begin to run thin, but here's some things we could still try:

  a) Try executing "app_process" directly, but through the native
     library. This is very unlikely to make a difference though.

  b) Try to track down the problem and fix it. For starters, is it really
     Superuser Whitelist, or does the standard su also have the same
     problem. In the latter case, we might even get Google to fix it.

  c) Try to write a tool that interacts with the PackageManager service in
     C, bypassing "app_process". For reference, have a look at
     'frameworks/base.git/servicemanager/binder.c.' Note also the
     /dev/binder device.

As a sidenote, a custom replacement for "pm" would also allow us to change
the component state without requirement the restart flag, which would
presumably be a lot faster.

In the meantime, we need to essentially require the user to have USB
Debugging enabled. There are however a few things we can do to help:

   - Again in the rare case that we are installed on the system
     partition, we can enable ADB (and re-disable it) automatically,
     because we have the right to write to the secure settings area.

   - If we don't have WRITE_SECURE_SETTINGS, we can use a "su setprop"
     call to change the ADB Debugging setting.

   - We make it clear to the user that ADB needs to be enabled for
     optional functioning via a bar on the top (this is currently not
     done, since auto-enabling ADB works well enough).

   - We use a timeout when running in action, so that when we can see
     that the state has changed, we simply stop waiting for the process
     to finish. This at least improves the user experience.
*/

/**
 * Takes care of toggling a component's state. This may take a
 * couple of seconds, so we use a thread.
 */
class ToggleTask extends ActivityAsyncTask<ListActivity, Object, Object, Boolean> {

	private ProgressDialog mPg;

	public ToggleTask(ListActivity wrapActivity) {
		super(wrapActivity);
	}

	public void connectTo(ListActivity wrappedObject) {
		super.connectTo(wrappedObject);

		// We are being unconnected from the current activity. Make sure
		// we reset any current progress dialog, so we don't think later
		// on that we have to cancel it; we don't need to bother canceling
		// it here either, because as the old activity is destroyed, so
		// is the progress dialog.
		if (wrappedObject == null) {
			mPg = null;
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mPg = new ProgressDialog(mWrapped);
		mPg.setIndeterminate(true);
		mPg.setMessage(mWrapped.getResources().getString(R.string.please_wait));
		mPg.setCancelable(false);
		mPg.show();
	}

	protected void processPostExecute(Boolean result) {
		if (mPg != null)
			mPg.cancel();

		if (!result)
			mWrapped.showDialog(ListActivity.DIALOG_STATE_CHANGE_FAILED);
		else {
			// We can reasonably expect that the component state
			// changed, so refresh the list.
			mWrapped.mListAdapter.notifyDataSetInvalidated();
		}
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// Cache the object locally, since the member might be reset
		// when the Activity disconnects.
		final ListActivity activity = mWrapped;

		ComponentInfo component = (ComponentInfo)params[0];
		// We could also read this right now, but we want to ensure
		// we always do the state change that we announced to the user
		// through the menu item caption (it's unlikely but possible
		// that the component state changed in the background while
		// the user decided what to do).
		Boolean doEnable = (Boolean)params[1];

		Log.i(ListActivity.TAG, "Asking package manger to "+
				"change component state to "+
				(doEnable ? "enabled": "disabled"));

		// As described above, in the rare case we are allowed to use
		// setComponentEnabledSetting(), we should do so.
		if (activity
				.checkCallingOrSelfPermission(permission.CHANGE_COMPONENT_ENABLED_STATE) == PackageManager.PERMISSION_GRANTED) {
			return Utils.setConponentEnable(activity, component, doEnable);
		} else {
			return Utils.setConponentEnableByRoot(activity, component, doEnable);
		}
	}

}