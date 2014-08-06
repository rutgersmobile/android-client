package edu.rutgers.css.Rutgers.items;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

import edu.rutgers.css.Rutgers.SettingsActivity;
import edu.rutgers.css.Rutgers.api.Analytics;

/**
 * Created by jamchamb on 8/5/14.
 */
public class AnalyticsEvent {

    private static final String TAG = "AnalyticsEvent";

    // Event types
    public static final String NEW_INSTALL = "fresh_launch";
    public static final String LAUNCH = "launch";
    public static final String ERROR = "error";
    public static final String CHANNEL_OPENED = "channel";
    public static final String DEFAULT_TYPE = "event";

    private String eventType;
    private String userRole;
    private String userCampus;
    private Timestamp timestamp;
    private JSONObject platformJSON;
    private JSONObject releaseJSON;

    public AnalyticsEvent(String eventType, Context context) {
        if(eventType == null) this.eventType = DEFAULT_TYPE;
        else this.eventType = eventType;

        if(context == null) {
            Log.w(TAG, "Null context given");
            this.userCampus = "null";
            this.userRole = "null";
        }
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            //TODO Translate these to full names
            this.userCampus = prefs.getString(SettingsActivity.KEY_PREF_HOME_CAMPUS, "null");
            this.userRole = prefs.getString(SettingsActivity.KEY_PREF_USER_TYPE, "null");
        }

        this.timestamp = new java.sql.Timestamp(Calendar.getInstance(Locale.US).getTime().getTime());

        this.platformJSON = Analytics.getPlatformJSON(context);
        this.releaseJSON = Analytics.getReleaseJSON(context);
    }

    public JSONObject getJSON() {
        JSONObject eventJSON = new JSONObject();

        try {
            eventJSON.put("type", eventType);
            eventJSON.put("role", userRole);
            eventJSON.put("campus", userCampus);
            eventJSON.put("date", timestamp.toString());
            eventJSON.put("platform", platformJSON);
        } catch (JSONException e) {
            Log.w(TAG, "getJSON(): " + e.getMessage());
        }

        return eventJSON;
    }

}
