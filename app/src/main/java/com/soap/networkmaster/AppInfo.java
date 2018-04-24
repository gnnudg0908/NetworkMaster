package com.soap.networkmaster;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable appIcon;
    private String appLabel;
    private String pkgName;
    private int uid;
    private boolean wifiState;
    private boolean mobileState;

    public AppInfo() {

    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable icon) {
        this.appIcon = icon;
    }


    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String label) {
        this.appLabel = label;
    }

    public String getPackageName() {
        return pkgName;
    }

    public void setPackageName(String packageName) {
        this.pkgName = packageName;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean getWifiState() {
        return wifiState;
    }

    public void setWifiState(boolean wifi) {
        this.wifiState = wifi;
    }

    public boolean getMobileState() {
        return mobileState;
    }

    public void setMobileState(boolean mobile) {
        this.mobileState = mobile;
    }
}
