package edu.rutgers.css.Rutgers.utils;

import android.util.Log;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.Config;

public final class LogUtils {

    private LogUtils() {}

    public static void LOGV(String tag, String message) {
        if (BuildConfig.DEBUG || Config.FORCE_DEBUG_LOGGING) {
            Log.v(tag, message);
        }
    }

    public static void LOGV(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG || Config.FORCE_DEBUG_LOGGING) {
            Log.v(tag, message, throwable);
        }
    }

    public static void LOGD(String tag, String message) {
        if (BuildConfig.DEBUG || Config.FORCE_DEBUG_LOGGING) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG || Config.FORCE_DEBUG_LOGGING) {
            Log.d(tag, message, throwable);
        }
    }

    public static void LOGI(String tag, String message) {
        Log.i(tag, message);
    }

    public static void LOGI(String tag, String message, Throwable throwable) {
        Log.i(tag, message, throwable);
    }

    public static void LOGW(String tag, String message) {
        Log.w(tag, message);
    }

    public static void LOGW(String tag, String message, Throwable throwable) {
        Log.w(tag, message, throwable);
    }

    public static void LOGE(String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }

    public static void LOGWTF(String tag, String message) {
        Log.wtf(tag, message);
    }

    public static void LOGWTF(String tag, String message, Throwable throwable) {
        Log.wtf(tag, message, throwable);
    }

}
