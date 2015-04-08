package edu.rutgers.css.Rutgers.utils;

import android.util.Log;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.Config;

public class LogUtils {

    private LogUtils() {}

    public static void LOGV(String tag, String message) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.v(tag, message);
        }
    }

    public static void LOGV(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.v(tag, message, throwable);
        }
    }

    public static void LOGD(String tag, String message) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.d(tag, message, throwable);
        }
    }

    public static void LOGI(String tag, String message) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void LOGI(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.i(tag, message, throwable);
        }
    }

    public static void LOGW(String tag, String message) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void LOGW(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.w(tag, message, throwable);
        }
    }

    public static void LOGE(String tag, String message) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void LOGE(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG && Config.LOGGING_ENABLED) {
            Log.e(tag, message, throwable);
        }
    }

}
