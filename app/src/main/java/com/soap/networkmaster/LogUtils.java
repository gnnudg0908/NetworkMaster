package com.soap.networkmaster;

import android.text.TextUtils;

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