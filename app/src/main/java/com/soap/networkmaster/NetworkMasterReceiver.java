package com.soap.networkmaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class NetworkMasterReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkMasterReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(action);
            serviceIntent.setClass(context, NetworkMasterService.class);
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                LogUtils.d(TAG, "[onReceive] ACTION_BOOT_COMPLETED");
            } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                LogUtils.d(TAG, "[onReceive] ACTION_SHUTDOWN");
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                final int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                final String packageName = getPackageName(intent);
                LogUtils.d(TAG, "[onReceive] ACTION_PACKAGE_REMOVED, packageName=" + packageName + " uid=" + uid);
                serviceIntent.putExtra(IntentUtils.EXTRA_PKG, packageName);
                serviceIntent.putExtra(IntentUtils.EXTRA_UID, uid);
            } else {
                return;
            }
            context.startService(serviceIntent);
        }
    }

    private String getPackageName(Intent intent) {
        if (intent == null) {
            return null;
        } else {
            Uri uri = intent.getData();
            return uri != null ? uri.getSchemeSpecificPart() : null;
        }
    }
}
