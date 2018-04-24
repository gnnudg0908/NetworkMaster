
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

import android.graphics.drawable.Drawable;

/**
 * @author soap
 * @github https://github.com/gnnudg0908/
 * @QQ 695144933
 * Date: 2018/4/24
 */
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
