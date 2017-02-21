package com.itel.networkcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InitReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkControl";
    
    protected static final String BOOT = "com.itel.action.BOOT"; //开机
    protected static final String SHUTDOWN = "com.itel.action.SHUTDOWN"; //关机
    protected static final String REMOVE = "com.itel.action.PACKAGE_REMOVED"; //卸载应用
    protected static final String INIT_FW = "com.itel.action.INIT_FW"; //初始化FW
    
    protected static final String EXTRA_PKG = "extra_package_name";
    protected static final String EXTRA_UID = "extra_uid";
    
    
	@Override
	public void onReceive(Context context, Intent intent) {
		if (null == intent) return;
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			Log.d(TAG, "InitReceiver receive ACTION_BOOT_COMPLETED, start InitService");
			Intent bootIntent = new Intent();
			bootIntent.setAction(BOOT);
			bootIntent.setClass(context, InitService.class);
			context.startService(bootIntent);
		} else if (Intent.ACTION_SHUTDOWN.equals(action)) {
			Intent shutdownIntent = new Intent();
			shutdownIntent.setAction(SHUTDOWN);
			shutdownIntent.setClass(context, InitService.class);
			context.startService(shutdownIntent);
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
			String tempStr = intent.getDataString();
			String packageName = tempStr.substring(8, tempStr.length());
			Log.d(TAG, "InitReceiver receive ACTION_PACKAGE_REMOVED, packageName=" + packageName + " uid=" + uid);
			Intent removeIntent = new Intent();
			removeIntent.setAction(REMOVE);
			removeIntent.setClass(context, InitService.class);
			removeIntent.putExtra(EXTRA_PKG, packageName);
			removeIntent.putExtra(EXTRA_UID, uid);
			context.startService(removeIntent);
		} 
	}

}
