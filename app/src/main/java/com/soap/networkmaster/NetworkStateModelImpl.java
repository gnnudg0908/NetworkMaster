
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.util.List;

/**
 * @author soap
 * @github https://github.com/gnnudg0908/
 * @QQ 695144933
 * Date: 2018/4/24
 */
public class NetworkStateModelImpl implements INetworkStateModel {
    private final static String TAG = "NetworkStateModelImpl";

    @Override
    public void initFirewall(Context context) {
        List<AppInfo> appInfos = Utils.getAppInfos(context, Utils.FILTER_ALL_APP);
        INetworkManagementService networkService = INetworkManagementService.Stub
                .asInterface(ServiceManager.getService("network_management"/*Context.NETWORKMANAGEMENT_SERVICE*/));
        boolean wifi;
        boolean mobile;
        for (AppInfo appInfo : appInfos) {
            int uid = appInfo.getUid();
            wifi = readNetworkState(context, appInfo.getPackageName(),
                    appInfo.getUid(), Utils.TYPE_WIFI);
            mobile = readNetworkState(context, appInfo.getPackageName(),
                    appInfo.getUid(), Utils.TYPE_MOBILE);
            appInfo.setMobileState(mobile);
            appInfo.setWifiState(wifi);
            LogUtils.d(TAG, "[initFirewall] wifi=" + wifi + "  mobile=" + mobile);
            /*
             * 如果是要禁止掉某个APP访问网络的话，
             * 应该是要下allow(true),而不是下deny(false)，
             * deny(false)是不禁止，allow(true)是允许禁止
             */
            try {
                networkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, true);
                if (wifi) {
                    networkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, false);
                }

                networkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, true);
                if (mobile) {
                    networkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, false);
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "[initFirewall]  connected to NetworkManagementService failed");
            }
        }
    }

    @Override
    public void saveFireWallInitFlag(Context context, boolean enable) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(Utils.SHARED_PF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Utils.INIT_FW_KEY, enable);
        editor.commit();
    }

    @Override
    public boolean readFireWallInitFlag(Context context) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(Utils.SHARED_PF, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(Utils.INIT_FW_KEY, false);
    }

    @Override
    public void saveNetworkState(Context context, String pkgName, int uid, String type, boolean enable) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(Utils.SHARED_PF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String key = pkgName + "_" + uid + "_" + type;
        editor.putBoolean(key, enable);
        editor.commit();
    }

    @Override
    public boolean readNetworkState(Context context, String pkgName, int uid, String type) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(Utils.SHARED_PF, Context.MODE_PRIVATE);
        String key = pkgName + "_" + uid + "_" + type;
        return sharedPref.getBoolean(key, false);
    }

    @Override
    public void removeNetworkState(Context context, String pkgName, int uid) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(Utils.SHARED_PF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String wifiKey = pkgName + "_" + uid + "_" + Utils.TYPE_WIFI;
        String mobileKey = pkgName + "_" + uid + "_" + Utils.TYPE_MOBILE;
        LogUtils.d(TAG, "removeData() wifiKey=" + wifiKey + "   mobileKey=" + mobileKey);
        editor.remove(wifiKey);
        editor.remove(mobileKey);
        editor.commit();
    }
}
