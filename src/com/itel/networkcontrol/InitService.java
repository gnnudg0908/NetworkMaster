package com.itel.networkcontrol;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class InitService extends Service {
	public static final String TAG = "NetworkControl";
	private Utils mUtils;
	private INetworkManagementService mNetworkService;
	private List<AppInfo> mAppList;

	@Override
	public void onCreate() {
		super.onCreate();
		mUtils = new Utils(this);
		mNetworkService = INetworkManagementService.Stub
				.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
		//mAppList = mUtils.queryFilterAppInfo(Utils.FILTER_THIRD_APP);
		mAppList = mUtils.queryFilterAppInfo(Utils.FILTER_ALL_APP);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent) {
			String action = intent.getAction();
			Log.d(TAG, "InitService onStartCommand()--> action=" + action);
			switch (action) {
			case InitReceiver.BOOT:
				initFirewall();
				mUtils.saveInitFwFlag(true);
				break;
			case InitReceiver.SHUTDOWN:
				mUtils.saveInitFwFlag(false);
				break;
			case InitReceiver.INIT_FW:
				initFirewall();
				mUtils.saveInitFwFlag(true);
				break;
			case InitReceiver.REMOVE:
				String pkg = intent.getStringExtra(InitReceiver.EXTRA_PKG);
				int uid = intent.getIntExtra(InitReceiver.EXTRA_UID, -1);
				mUtils.removeData(pkg, uid);
				break;
			default:
				break;
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void initFirewall() {
		boolean wifi = false;
		boolean mobile = false;
		for (AppInfo appInfo : mAppList) {
			int uid = appInfo.getUid();
			wifi = appInfo.getWifiState();
			mobile = appInfo.getMobileState();
			Log.d(TAG, "InitService initData()--> wifi=" + wifi + "  mobile=" + mobile);
			try {
				mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, true);
				if (!wifi) {
					mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, false);
				}

				mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, true);
				if (!mobile) {
					mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, false);
				}
			} catch (RemoteException e) {
				Log.e(TAG, "InitService onStartCommand()-->  connected to NetworkManagementService failed");
			}
		}
	}
}
