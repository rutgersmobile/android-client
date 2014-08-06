package edu.rutgers.css.Rutgers.api;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.BuildConfig;

/**
 * Created by jamchamb on 8/5/14.
 */
public class Analytics {

    public static final String TAG = "Analytics";

    public static JSONObject getPlatformJSON(Context context) {
        JSONObject platformJSON = new JSONObject();
        try {
            platformJSON.put("os", AppUtil.OSNAME);
            platformJSON.put("version", Build.VERSION.RELEASE);
            platformJSON.put("model", Build.MANUFACTURER + " " + Build.MODEL);
            platformJSON.put("tablet", AppUtil.isTablet(context));
            platformJSON.put("android", Build.VERSION.SDK_INT);
            platformJSON.put("id", AppUtil.getUUID(context));
        } catch (JSONException e) {
            Log.w(TAG, "getPlatformJSON(): " + e.getMessage());
        }
        return platformJSON;
    }

    public static JSONObject getReleaseJSON(Context context) {
        JSONObject releaseJSON = new JSONObject();
        try {
            releaseJSON.put("debug", BuildConfig.DEBUG);
            releaseJSON.put("beta", AppUtil.BETA);
            releaseJSON.put("version", AppUtil.VERSION);
            releaseJSON.put("api", AppUtil.API_LEVEL);
        } catch (JSONException e) {
            Log.e(TAG, "getReleaseJSON(): " + e.getMessage());
        }
        return releaseJSON;
    }

}
