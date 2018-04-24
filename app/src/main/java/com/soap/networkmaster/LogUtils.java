
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

import android.text.TextUtils;

/**
 * @author soap
 * @github https://github.com/gnnudg0908/
 * @QQ 695144933
 * Date: 2018/4/24
 */
public final class LogUtils {
    private static final String APP_TAG = "NetworkMaster";
    private static final boolean LOG_DEBUG = isDebug();
    private static final boolean LOG_RELEASE = true;

    private static boolean isDebug() {
        String type = Utils.getSystemProperties("ro.build.type");
        return TextUtils.equals(type, "userdebug") || TextUtils.equals(type, "eng");
    }

    /**
     * Print log, level error
     * @param tag the tag of the class
     * @param msg the message to print
     */
    public static void e(String tag, String msg) {
        if (LOG_RELEASE) {
            android.util.Log.e(APP_TAG, tag + ", " + msg);
        }
    }

    /**
     * Print log, level warning
     *
     * @param tag the tag of the class
     * @param msg the message to print
     */
    public static void w(String tag, String msg) {
        if (LOG_RELEASE) {
            android.util.Log.w(APP_TAG, tag + ", " + msg);
        }
    }

    /**
     * Print log, level debug
     * @param tag the tag of the class
     * @param msg the message to print
     */
    public static void d(String tag, String msg) {
        if (LOG_RELEASE) {
            android.util.Log.d(APP_TAG, tag + ", " + msg);
        }
    }

    /**
     * Print log, level info
     *
     * @param tag the tag of the class
     * @param msg the message to print
     */
    public static void i(String tag, String msg) {
        if (LOG_DEBUG) {
            android.util.Log.i(APP_TAG, tag + ", " + msg);
        }
    }

    /**
     * Print log, level verbose
     *
     * @param tag the tag of the class
     * @param msg the message to print
     */
    public static void v(String tag, String msg) {
        if (LOG_DEBUG) {
            android.util.Log.v(APP_TAG, tag + ", " + msg);
        }
    }
}