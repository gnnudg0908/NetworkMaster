
/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.soap.networkmaster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * @author soap
 * @github https://github.com/gnnudg0908/
 * @QQ 695144933
 * Date: 2018/4/24
 */
public class Utils {
    private static final String TAG = "Utils";

    public static final int FILTER_ALL_APP = 0; // 所有应用程序
    public static final int FILTER_SYSTEM_APP = 1; // 系统程序
    public static final int FILTER_THIRD_APP = 2; // 第三方应用程序
    public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

    protected static final String TYPE_MOBILE = "mobile";
    protected static final String TYPE_WIFI = "wifi";

    protected static final int TYPE_MOBILE_ID = 0;
    protected static final int TYPE_WIFI_ID = 1;

    protected static final String SHARED_PF = "network_master";
    protected static final String INIT_FW_KEY = "init_fw";

    public static class AppLabelComparator implements Comparator<AppInfo> {
        private final Collator sCollator = Collator.getInstance();

        public AppLabelComparator() {

        }

        public final int compare(AppInfo appInfo1, AppInfo appInfo2) {
            String appLabel1 = appInfo1.getAppLabel();
            if (appLabel1 == null) {
                appLabel1 = appInfo1.getPackageName();
            }
            String appLabel2 = appInfo2.getAppLabel();
            if (appLabel2 == null) {
                appLabel2 = appInfo2.getPackageName();
            }
            return sCollator.compare(appLabel1, appLabel2);
        }
    }

    // 根据查询条件，查询特定的ApplicationInfo
    public static List<AppInfo> getAppInfos(Context context, int filter) {
        PackageManager pm = context.getPackageManager();
        // 查询所有已经安装的应用程序
        List<PackageInfo> pkgInfoList = pm.getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
        //List<ApplicationInfo> applicationInfos = new ArrayList<>();
        List<AppInfo> appInfos = new ArrayList<>(); // 保存过滤查到的AppInfo
        for (PackageInfo packageInfo : pkgInfoList) {
            String[] premissions = packageInfo.requestedPermissions;
            if (premissions != null && premissions.length > 0) {
                for (String premission : premissions) {
                    if ("android.permission.INTERNET".equals(premission)) {
                        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                        //applicationInfos.add(packageInfo.applicationInfo);
                        switch (filter) {
                            case FILTER_ALL_APP: // 所有应用程序
                                appInfos.add(getAppInfo(context, pm, applicationInfo));
                                continue;
                            case FILTER_SYSTEM_APP: // 系统程序
                                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                    appInfos.add(getAppInfo(context, pm, applicationInfo));
                                }
                                continue;
                            case FILTER_THIRD_APP: // 第三方应用程序
                                // 非系统程序
                                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                                    appInfos.add(getAppInfo(context, pm, applicationInfo));
                                    LogUtils.d(TAG, "FILTER_THIRD_APP packageName=" + applicationInfo.packageName);
                                }
                                // 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                                else if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                                    appInfos.add(getAppInfo(context, pm, applicationInfo));
                                    LogUtils.d(TAG, "FILTER_THIRD_APP packageName=" + applicationInfo.packageName);
                                }
                                continue;
                            case FILTER_SDCARD_APP: // 安装在SDCard的应用程序
                                if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                                    appInfos.add(getAppInfo(context, pm, applicationInfo));
                                }
                                continue;
                            default:
                                LogUtils.e(TAG, "unknown filter=" + filter);

                        }

                    }
                }
            }
        }
        //Log.d(TAG, "applicationInfos.size()=" + applicationInfos.size());
        // 排序
        //Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(pm));
        Collections.sort(appInfos, new AppLabelComparator());
        LogUtils.d(TAG, "appInfos.size()=" + appInfos.size());
        return appInfos;
    }

    /*
     * 创建AppInfo对象，设置图标、名称、包名、wifi和移动数据状态
     */
    private static AppInfo getAppInfo(Context context, PackageManager pm, ApplicationInfo app) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppIcon((Drawable) pm.getApplicationIcon(app));
        appInfo.setAppLabel((String) pm.getApplicationLabel(app));
        appInfo.setPackageName(app.packageName);
        appInfo.setUid(app.uid);
        appInfo.setWifiState(true);
        appInfo.setMobileState(true);
        return appInfo;
    }

    public static String getNetworkTypeById(int id) {
        switch (id) {
            case TYPE_MOBILE_ID:
                return TYPE_MOBILE;
            case TYPE_WIFI_ID:
                return TYPE_WIFI;
            default:
                return null;
        }
    }

    public static int getNetworkIdByType(String type) {
        switch (type) {
            case TYPE_MOBILE:
                return TYPE_MOBILE_ID;
            case TYPE_WIFI:
                return TYPE_WIFI_ID;
            default:
                return -1;
        }
    }

    public static String getSystemProperties(String key, String defaultValue) throws IllegalArgumentException {
        String value = defaultValue;
        try {
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method get = SystemProperties.getMethod("get", String.class, String.class);
            value = (String) get.invoke(null, key, defaultValue);
        } catch (ClassNotFoundException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (NoSuchMethodException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (IllegalAccessException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (InvocationTargetException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        }
        return value;
    }

    public static String getSystemProperties(String key) throws IllegalArgumentException {
        return getSystemProperties(key, "");
    }

    public static int getSystemPropertiesInt(String key, int defaultValue) throws IllegalArgumentException {
        int value = defaultValue;
        try {
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method getInt = SystemProperties.getMethod("getInt", String.class, int.class);
            value = (int) getInt.invoke(null, key, defaultValue);
        } catch (ClassNotFoundException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (NoSuchMethodException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (IllegalAccessException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        } catch (InvocationTargetException e) {
            LogUtils.e(TAG, "Exception: " + e.toString());
        }
        return value;
    }
}
