
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

/**
 * @author soap
 * @github https://github.com/gnnudg0908/
 * @QQ 695144933
 * Date: 2018/4/24
 */
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
