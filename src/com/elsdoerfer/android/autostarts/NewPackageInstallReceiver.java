package com.elsdoerfer.android.autostarts;

import java.util.ArrayList;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;
import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NewPackageInstallReceiver extends BroadcastReceiver {

	protected static final String TAG = "NewPackageInstallReceiver";
	public static final String EXTRA_RECEIVERS = "extra.receivers";
	private static ArrayList<ComponentInfo> mReiceivers;

	@Override
	public void onReceive(final Context context, Intent intent) {
//		Bundle extras = intent.getExtras();
//		int uid = extras.getInt(Intent.EXTRA_UID);
		String pkgName = intent.getDataString();
		pkgName = pkgName.substring("package:".length(), pkgName.length());
		Log.d(TAG, "in onReceive: pkgName=" + pkgName);
		boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
		if (replacing) {
			Log.i(TAG, "in onReceive, ignore package replaced! ");
			return;
		}

		mReiceivers = new ArrayList<ComponentInfo>();
		PackageInfo pkgInfo = null;
		final PackageManager pm = context.getPackageManager();
		try {
			pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_DISABLED_COMPONENTS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return;
		}
		final PackageInfo p = pkgInfo;
		new ReceiverReader(context, new ReceiverReader.OnLoadProgressListener() {
			
			@Override
			public void onProgress(float progress) {
			}
			
			@Override
			public void onNewReceiver(ComponentInfo receiver) {
				// TODO Auto-generated method stub
				Log.d(TAG, "in onNewReceiver: receiver=" + receiver.componentName);
				mReiceivers.add(receiver);
			}
			
			@Override
			public void onNewIntentFilterInfo(IntentFilterInfo info) {
			}
		}).parsePackage(pkgInfo);
		
		if (mReiceivers.size() > 0) {
			String receivers = "";
			for (ComponentInfo receiver: mReiceivers) {
				receivers += receiver.getLabel() + " ";
			}
			Intent notifyIntent = new Intent(context, NewInstalledAppActivity.class);
//			notifyIntent.putExtra(EXTRA_RECEIVERS, mReiceivers);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
			
			NotificationCompat.Builder builder =
			        new NotificationCompat.Builder(context)
			        .setSmallIcon(R.drawable.icon)
			        .setContentTitle(p.applicationInfo.loadLabel(pm) + " has " + mReiceivers.size() + " receivers")
			        .setAutoCancel(true)
			        .setContentText(receivers)
			        .setContentIntent(pendingIntent);
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(pkgName.hashCode(), builder.build());
		}
	}
	
	public static ArrayList<ComponentInfo> getReceivers() {
		return mReiceivers;
	}

}
