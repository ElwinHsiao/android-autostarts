package com.elsdoerfer.android.autostarts;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;

import android.Manifest.permission;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class Utils {
	/**
	 * It's unbelievable how difficult it is in Java to read a stupid
	 * stream into a string.
	 *
	 * From:
	 * 	 http://stackoverflow.com/questions/309424/in-java-how-do-a-read-an-input-stream-in-to-a-string
	 */
	static String readStream(InputStream stream) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(stream, "UTF-8");
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read>0)
				out.append(buffer, 0, read);
		} while (read>=0);
		return out.toString();
	}

	/**
	 * Stupid Java's LinkedHashMap has no indexOf() method.
	 */
	static int getHashMapIndex(LinkedHashMap<?, ?> map, Object search) {
		Set<?> keys = map.keySet();
		Iterator<?> i = keys.iterator();
		Object curr;
		int count = -1;
		do {
			curr = i.next();
			count++;
			if (curr.equals(search))
				return count;
		}
		while (i.hasNext());
		return -1;
	}

	/**
	 * Determine the path of the su executable.
	 *
	 * The emulator and ADP1 device both have a su binary in
	 * /system/xbin/su, but it doesn't allow apps to use it (su app_29
	 * $ su su: uid 10029 not allowed to su).
	 *
	 * Cyanogen used to have su in /system/bin/su, in newer versions
	 * it's a symlink to /system/xbin/su.
	 *
	 * The Archos tablet has it in /data/bin/su, since they don't have
	 * write access to /system yet.
	 */
	static String[] SU_OPTIONS =  {
		"/data/bin/su",
		"/system/bin/su",
		// This is last because we are afraid a proper su might be in
		// one of those other locations, while this one is secured.
		"/system/xbin/su",
	};
	static String getSuPath() {
		for (String p : SU_OPTIONS) {
			File su = new File(p);
			if (su.exists()) {
				Log.d(ListActivity.TAG, "su found at: "+p);
				return p;
			}
			else
				if (ListActivity.LOGV)
					Log.v(ListActivity.TAG, "No su in: "+p);
		}
		Log.d(ListActivity.TAG, "No su found in a well-known location, "+
				"will just use \"su\".");
		return "su";
	}

	/*
	 * Running an app through root isn't even that straightforward as
	 * it would seem. Here's some issues we've run into so far, and which
	 * we try to workaround here:
	 *
	 *  1) The Superuser Whitelist application most rooted devices
	 *     use identifies the command based on arguments. If we
	 *     were to just call su -c "pm xyz", the user would need
	 *     to confirm every single call; "Allows allow" would be
	 *     useless.
	 *
	 *  2) Rarely, a devices seems to have a su-executable that uses a
	 *     different argument syntax (`su -c "command args"` vs.
	 *     `su -c command args`).
	 *
	 *  3) Some ROMs have their "su" in a non-standard location, like
	 *     Archos in /data/bin, and it's not on the path either (this
	 *     is because they don't have write-access to /system yet).
	 *
	 *  4) Some custom ROMs contain what seems to be a kernel bug,
	 *     in which su/sh?, when run outside of the system shell,
	 *     cannot access certain paths. The error is "pm: not found".
	 *     This happens even though the file does exists, and runs
	 *     just fine from the shell.
	 *
	 * The common approach chosen by most root apps seems to be to
	 * run "su" and pipe commands into it. This will solve (1) and (2).
	 * (3) we solve by checking multiple locations for su.
	 * We'll still have to see about (4).
	 */
	static boolean runRootCommand(String command, String[] env,
			Integer timeout)
	{
		Process process = null;
		DataOutputStream os = null;
		try {
			Log.d(ListActivity.TAG, String.format(
					"Running '%s' as root, timeout=%s", command, timeout));

			process = runWithEnv(getSuPath(), env);
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command+"\n");
			os.writeBytes("echo \"rc:\" $?\n");
			os.writeBytes("exit\n");
			os.flush();

			// Handle a requested timeout, or just use waitFor() otherwise.
			if (timeout != null) {
				long finish = System.currentTimeMillis() + timeout;
				while (true) {
					Thread.sleep(300);
					if (!isProcessAlive(process))
						break;
					// TODO: We could use a callback to let the caller
					// check the success-condition (like the state properly
					// being changed), and then end early, rather than
					// waiting for the timeout to occur. However, this
					// is made more complicated by us not really wanting
					// to kill a process early that would never have hung,
					// but which might not actually be completely finished yet
					// when the callback would register success.
					// Also, now that the timeout is only used as a last-resort
					// mechanism anyway, with most cases of a hanging process
					// being avoided by switching on ADB Debugging, improving
					// the timeout handling isn't that important anymore.
					if (System.currentTimeMillis() > finish) {
						// Usually, this can't be considered a success.
						// However, in terms of the bug we're trying to
						// work around here (the call hanging if adb
						// debugging is disabled), the command would
						// have successfully run, but just doesn't
						// return. We report success, just in case, and
						// the caller will have to check whether the
						// command actually did do it's job.
						// TODO: It might be core "correct" to return false
						// here, or indicate the timeout in some other way,
						// and let the caller ignore those values on their
						// own volition.
						Log.w(ListActivity.TAG, "Process doesn't seem "+
								"to stop on it's own, assuming it's hanging");
						// Note: 'finally' will call destroy(), but you
						// might still see zombies.
						return true;
					}
				}
			}
			else
			  process.waitFor();

			Log.d(ListActivity.TAG, "Process returned with "+process.exitValue());
			Log.d(ListActivity.TAG, "Process stdout was: "+
				Utils.readStream(process.getInputStream())+
				"; stderr: "+Utils.readStream(process.getErrorStream()));

			// In order to consider this a success, we require to
			// things: a) a proper exit value, and ...
			if (process.exitValue() != 0)
				return false;

			return true;

		} catch (FileNotFoundException e) {
			Log.e(ListActivity.TAG, "Failed to run command", e);
			return false;
		} catch (IOException e) {
			Log.e(ListActivity.TAG, "Failed to run command", e);
			return false;
		} catch (InterruptedException e) {
			Log.e(ListActivity.TAG, "Failed to run command", e);
			return false;
		}
		finally {
			if (os != null)
				try { os.close(); }
				catch (IOException e) { throw new RuntimeException(e); }
			if (process != null) {
				try {
					// Yes, this really is the way to check if the process is
					// still running.
					process.exitValue();
				} catch (IllegalThreadStateException e) {
					// Only call destroy() if the process is still running;
					// Calling it for a terminated process will not crash, but
					// (starting with at least ICS/4.0) spam the log with INFO
					// messages ala "Failed to destroy process" and "kill
					// failed: ESRCH (No such process)".
					process.destroy();
				}
			}
		}
	}

	/**
	 * This code is adapted from java.lang.ProcessBuilder.start().
	 *
	 * The problem is that Android doesn't allow us to modify the
	 * map returned by ProcessBuilder.environment(), even though the
	 * docstring indicates that it should. This is because it simply
	 * returns the SystemEnvironment object that System.getenv() gives
	 * us. The relevant portion in the source code is marked as
	 * "// android changed", so presumably it's not the case in the
	 * original version of the Apache Harmony project.
	 *
	 * Note that simply passing the environment variables we want
	 * to Process.exec won't be good enough, since that would override
	 * the environment we inherited completely.
	 *
	 * We needed to be able to set a CLASSPATH environment variable for
	 * our new process in order to use the "app_process" command directly.
	 * Note: "app_process" takes arguments passed on to the Dalvik VM as
	 * well; this might be an alternative way to set the class path.
	 */
	public static Process runWithEnv(String command, String[] customEnv) throws IOException {
		Map<String, String> environment = System.getenv();
	    String[] envArray = new String[environment.size()+
	                                   (customEnv != null ? customEnv.length : 0)];
	    int i = 0;
	    for (Map.Entry<String, String> entry : environment.entrySet())
	        envArray[i++] = entry.getKey() + "=" + entry.getValue();
	    if (customEnv != null)
		    for (String entry : customEnv)
		        envArray[i++] = entry;
	    Process process = Runtime.getRuntime().exec(command, envArray);
	    return process;
	}

	/**
	 * Check whether a process is still alive. We use this as a naive
	 * way to implement timeouts.
	 */
	public static boolean isProcessAlive(Process p) {
	    try {
	        p.exitValue();
	        return false;
	    } catch (IllegalThreadStateException e) {
	        return true;
	    }
	}

	/**
	 * Sleep for a while; without dealing with the exception.
	 */
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	public static Boolean setConponentEnable(final Activity activity,
			ComponentInfo component, Boolean doEnable) {
		Log.i(ListActivity.TAG, "Calling setComponentEnabledState() directly");
		PackageManager pm = activity.getPackageManager();
		ComponentName c = new ComponentName(component.packageInfo.packageName,
				component.componentName);
		pm.setComponentEnabledSetting(c,
				doEnable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
						: PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
		component.currentEnabledState = pm.getComponentEnabledSetting(c);
		return (component.isCurrentlyEnabled() == doEnable);
	}

	public static Boolean setConponentEnableByRoot(final Activity activity,
			ComponentInfo component, Boolean doEnable) {
		Log.i(ListActivity.TAG, "Changing state by employing root access");

		ContentResolver cr = activity.getContentResolver();
		boolean adbNeedsRedisable = false;
		boolean adbEnabled;
		try {
			adbEnabled = (Settings.Secure.getInt(cr,
					Settings.Secure.ADB_ENABLED) == 1);
		} catch (SettingNotFoundException e) {
			// This started to happen at times on the ICS emulator
			// (and possibly one user reported it).
			Log.w(ListActivity.TAG,
					"Failed to read adb_enabled setting, assuming no", e);
			adbEnabled = false;
		}

		// If adb is disabled, try to enable it, temporarily. This will
		// make our root call go through without hanging.
		// TODO: It seems this might no longer be required under ICS.
		if (!adbEnabled) {
			Log.i(ListActivity.TAG, "Switching ADB on for the root call");
			if (setADBEnabledState(activity, cr, true)) {
				adbEnabled = true;
				adbNeedsRedisable = true;
				// Let's be extra sure we don't run into any timing-related
				// hiccups.
				Utils.sleep(1000);
			}
		}

		try {
			// Run the command; we have different invocations we can try, but
			// we'll stop at the first one we succeed with.
			//
			// On ICS, it became necessary to set a library path (which is
			// cleared for suid programs, for obvious reasons). It can't hurt
			// on older versions. See also
			// https://github.com/ChainsDD/su-binary/issues/6
			final String libs = "LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:/system/lib\" ";
			boolean success = false;
			for (String[] set : new String[][] {
					{ libs + "pm %s '%s/%s'", null },
					{ libs + "sh /system/bin/pm %s '%s/%s'", null },
					{ libs + "app_process /system/bin com.android.commands.pm.Pm %s '%s/%s'", "CLASSPATH=/system/framework/pm.jar" },
					{ libs + "/system/bin/app_process /system/bin com.android.commands.pm.Pm %s '%s/%s'", "CLASSPATH=/system/framework/pm.jar" }, }) {
				if (Utils.runRootCommand(String.format(set[0],
						(doEnable ? "enable" : "disable"),
						component.packageInfo.packageName,
						component.componentName),
						(set[1] != null) ? new String[] { set[1] } : null,
						// The timeout shouldn't really be needed ever, since
						// we now automatically enable ADB, which should work
						// around any freezing issue. However, in rare, hard
						// to reproduce cases, it still occurs, and in those
						// cases the timeout will improve the user experience.
						25000)) {
					success = true;
					break;
				}
			}

			// We are happy if both the command itself succeed (return code)...
			if (!success)
				return false;

			// ...and the state should now actually be what we expect.
			// TODO: It would be more stable if we would reload
			// getComponentEnabledSetting() regardless of the return code.
			PackageManager pm = activity.getPackageManager();
			component.currentEnabledState = getComponentEnabled(component, pm);

			success = component.isCurrentlyEnabled() == doEnable;
			if (success)
				Log.i(ListActivity.TAG, "State successfully changed");
			else
				Log.i(ListActivity.TAG, "State change failed");
			return success;
		} finally {
			if (adbNeedsRedisable) {
				Log.i(ListActivity.TAG, "Switching ADB off again");
				setADBEnabledState(activity, cr, false);
				// Delay releasing the GUI for a while, there seems to
				// be a mysterious problem of repeating this process multiple
				// times causing it to somehow lock up, no longer work.
				// I'm hoping this might help.
				Utils.sleep(5000);
			}
		}
	}

	public static int getComponentEnabled(ComponentInfo component, PackageManager pm) {
		ComponentName c = new ComponentName(component.packageInfo.packageName, component.componentName);
		return pm.getComponentEnabledSetting(c);
	}

	/**
	 * Enable/Disable the "ADB Debugging" setting. We do this either by
	 * employing the WRITE_SECURE_SETTINGS permission, if we have it, or by
	 * using a root call.
	 * @param activity 
	 */
	private static boolean setADBEnabledState(Activity activity, ContentResolver cr, boolean enable) {
		if (activity.checkCallingOrSelfPermission(permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
			Log.i(ListActivity.TAG,
					"Using secure settings API to touch ADB setting");
			return Settings.Secure.putInt(cr, Settings.Secure.ADB_ENABLED,
					enable ? 1 : 0);
		} else {
			Log.i(ListActivity.TAG, "Using setprop call to touch ADB setting");
			return Utils.runRootCommand(String.format(
					"setprop persist.service.adb.enable %s", enable ? 1 : 0),
					null, null);
		}
	}
}
