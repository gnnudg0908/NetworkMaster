package com.soap.networkmaster;

import android.content.Context;

public interface INetworkStateModel {

    /**
     * 初始化网络防火墙
     */
    void initFirewall(Context context);

    /**
     * 保存网络防火墙初始化标志
     */
    void saveFireWallInitFlag(Context context, boolean enable);

    /**
     * 读取网络防火墙初始化标志
     */
    boolean readFireWallInitFlag(Context context);

    /**
     * 保存应用网络防火墙状态
     */
    void saveNetworkState(Context context, String pkgName, int uid, String type, boolean enable);

    /**
     * 读取应用网络防火墙状态
     */
    boolean readNetworkState(Context context, String pkgName, int uid, String type);

    /**
     * 删除应用网络防火墙状态
     */
    void removeNetworkState(Context context, String pkgName, int uid);

}
