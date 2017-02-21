package com.itel.networkcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Utils {
	public static final String TAG = "NetworkControl";

	protected static final int FILTER_ALL_APP = 0; // 所有应用程序
	protected static final int FILTER_SYSTEM_APP = 1; // 系统程序
	protected static final int FILTER_THIRD_APP = 2; // 第三方应用程序
	protected static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

	protected static final String TYPE_WIFI = "wifi";
	protected static final String TYPE_MOBILE = "mobile";
	protected static final int TYPE_WIFI_ID = 1;
	protected static final int TYPE_MOBILE_ID = 0;

	protected static final String SHARED_PF = "network_control";
	protected static final String INIT_FW_KEY = "init_fw";
	private Context mContext;
	private PackageManager mPM;
	private SharedPreferences mSharedPref;

	public Utils(Context context) {
		mContext = context.getApplicationContext();
		mPM = mContext.getPackageManager();
		mSharedPref = mContext.getSharedPreferences(SHARED_PF, Context.MODE_PRIVATE);
	}

	// 根据查询条件，查询特定的ApplicationInfo
	public List<AppInfo> queryFilterAppInfo(int filter) {
		// 查询所有已经安装的应用程序
		List<PackageInfo> pkgInfoList = mPM
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
		List<ApplicationInfo> appcationInfoList = new ArrayList<ApplicationInfo>();
		for (PackageInfo info : pkgInfoList) {
			String[] premissions = info.requestedPermissions;
			if (premissions != null && premissions.length > 0) {
				for (String premission : premissions) {
					if ("android.permission.INTERNET".equals(premission)) {
						appcationInfoList.add(info.applicationInfo);
					}
				}
			}
		}
		Log.d(TAG, "appcationInfoList.size()=" + appcationInfoList.size());
		Collections.sort(appcationInfoList, new ApplicationInfo.DisplayNameComparator(mPM));// 排序
		List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo
		// 根据条件来过滤
		switch (filter) {
		case FILTER_ALL_APP: // 所有应用程序
			appInfos.clear();
			for (ApplicationInfo app : appcationInfoList) {
				appInfos.add(getAppInfo(app));
			}
			return appInfos;
		case FILTER_SYSTEM_APP: // 系统程序
			appInfos.clear();
			for (ApplicationInfo app : appcationInfoList) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		case FILTER_THIRD_APP: // 第三方应用程序
			appInfos.clear();
			for (ApplicationInfo app : appcationInfoList) {
				// 非系统程序
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					appInfos.add(getAppInfo(app));
					Log.d(TAG, "FILTER_THIRD_APP packageName=" + app.packageName);
				}
				// 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
				else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					appInfos.add(getAppInfo(app));
					Log.d(TAG, "FILTER_THIRD_APP packageName=" + app.packageName);
				}
			}
			break;
		case FILTER_SDCARD_APP: // 安装在SDCard的应用程序
			appInfos.clear();
			for (ApplicationInfo app : appcationInfoList) {
				if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		default:
			return null;
		}
		Log.d(TAG, "appInfos.size()=" + appInfos.size());
		return appInfos;
	}

	/*
	 * 创建AppInfo对象，设置图标、名称、包名、wifi和移动数据状态
	 */
	private AppInfo getAppInfo(ApplicationInfo app) {
		AppInfo appInfo = new AppInfo();
		appInfo.setAppIcon((Drawable) mPM.getApplicationIcon(app));
		appInfo.setAppLabel((String) mPM.getApplicationLabel(app));
		String pkgName = app.packageName;
		appInfo.setPackageName(pkgName);
		int uid = app.uid;
		appInfo.setUid(uid);
		boolean wifi = readData(pkgName, uid, TYPE_WIFI);
		boolean mobile = readData(pkgName, uid, TYPE_MOBILE);
		Log.d(TAG, "pkgName=" + pkgName + "; uid=" + uid + "; wifi=" + wifi + "; mobile=" + mobile);
		appInfo.setWifiState(wifi);
		appInfo.setMobileState(mobile);
		return appInfo;
	}

	/*
	 * 将状态保存到SharedPreferences
	 */
	public void saveData(String pkgName, int uid, String type, boolean enable) {
		SharedPreferences.Editor editor = mSharedPref.edit();
		String key = pkgName + "_" + uid + "_" + type;
		editor.putBoolean(key, enable);
		editor.commit();
	}

	/*
	 * 从SharedPreferences读取状态
	 */
	public boolean readData(String pkgName, int uid, String type) {
		String key = pkgName + "_" + uid + "_" + type;
		return mSharedPref.getBoolean(key, false);
	}

	public void removeData(String pkgName, int uid) {
		SharedPreferences.Editor editor = mSharedPref.edit();
		String wifiKey = pkgName + "_" + uid + "_" + TYPE_WIFI;
		String mobileKey = pkgName + "_" + uid + "_" + TYPE_MOBILE;
		Log.d(TAG, "removeData() wifiKey=" + wifiKey + "   mobileKey=" + mobileKey);
		editor.remove(wifiKey);
		editor.remove(mobileKey);
		editor.commit();
	}

	public void saveInitFwFlag(boolean enable) {
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putBoolean(INIT_FW_KEY, enable);
		editor.commit();
	}

	public boolean readInitFwFlag() {
		return mSharedPref.getBoolean(INIT_FW_KEY, false);
	}
}
