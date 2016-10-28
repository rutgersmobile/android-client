package edu.rutgers.css.Rutgers.api;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.model.AnalyticsOpenHelper;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * Analytics service. Queues analytics events and flushes them to the server when the app is paused.
 * <p>
 * The events are stored in a local SQLite database until they are successfully flushed to the server.
 * @author James Chambers
 */
public final class Analytics extends IntentService {

    public static final String TAG                  = "Analytics";

//    private static final String POST_URL = "http://sauron.rutgers.edu/~jamchamb/analytics.php"; // TODO Replace
    private static final String POST_URL = Config.API_BASE + "analytics.php";

    // Event types
    public static final String NEW_INSTALL          = "fresh_launch";
    public static final String LAUNCH               = "launch";
    public static final String ERROR                = "error";
    public static final String CHANNEL_OPENED       = "channel";
    public static final String DEFAULT_TYPE         = "event";

    private static final int QUEUE_MODE = 0;
    private static final int POST_MODE = 1;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public Analytics() {
        super("Analytics");
    }

    public static void sendChannelEvent(Context context, @NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("handle", args.getString(ComponentFactory.ARG_COMPONENT_TAG));
            extras.put("url", args.getString(ComponentFactory.ARG_URL_TAG));
            extras.put("api", args.getString(ComponentFactory.ARG_API_TAG));
            extras.put("title", args.getString(ComponentFactory.ARG_TITLE_TAG));
        } catch (JSONException e) {
            LOGE(TAG, Log.getStackTraceString(e));
        }
        queueEvent(context, Analytics.CHANNEL_OPENED, extras);
    }

    public static void sendChannelErrorEvent(Context context, @NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("description","failed to open channel");
            extras.put("handle", args.getString(ComponentFactory.ARG_COMPONENT_TAG));
            extras.put("url", args.getString(ComponentFactory.ARG_URL_TAG));
            extras.put("api", args.getString(ComponentFactory.ARG_API_TAG));
            extras.put("title", args.getString(ComponentFactory.ARG_TITLE_TAG));
        } catch (JSONException e) {
            LOGE(TAG, Log.getStackTraceString(e));
        }
        queueEvent(context, Analytics.ERROR, extras);
    }

    /**
     * Queue an analytics event.
     * @param context App context
     * @param eventType Event type
     * @param extra Extra string fields, contained in JSON object
     */
    public static void queueEvent(@NonNull Context context, @NonNull String eventType, @Nullable JSONObject extra) {
        Intent analyticsIntent = new Intent(context, Analytics.class);
        analyticsIntent.putExtra("mode", QUEUE_MODE);
        analyticsIntent.putExtra("type", eventType);
        if (extra != null) analyticsIntent.putExtra("extra", extra.toString());
        context.startService(analyticsIntent);
    }

    /**
     * Tell the service to flush all analytics events to the server.
     * @param context App context
     */
    public static void postEvents(Context context) {
        Intent analyticsIntent = new Intent(context, Analytics.class);
        analyticsIntent.putExtra("mode", POST_MODE);
        context.startService(analyticsIntent);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        int mode = workIntent.getIntExtra("mode", -1);

        switch(mode) {
            case QUEUE_MODE:
                doQueue(workIntent);
                break;

            case POST_MODE:
                doPost(workIntent);
                break;

            default:
                throw new IllegalArgumentException("Invalid mode supplied");
        }

    }

    /**
     * Queue the event in the SQLite database.
     * @param workIntent Work intent given to service
     */
    private void doQueue(Intent workIntent) {
        String eventType = workIntent.getStringExtra("type");
        String extraString = workIntent.getStringExtra("extra");

        if (eventType != null) {
            LOGV(TAG, "Queueing " + eventType + " event");

            // Open the event database
            AnalyticsOpenHelper analyticsOpenHelper = new AnalyticsOpenHelper(this);
            SQLiteDatabase database;
            try {
                database = analyticsOpenHelper.getWritableDatabase();
            } catch (SQLiteException sqle) {
                LOGE(TAG, sqle.getMessage());
                return;
            }

            // Set up values to insert
            ContentValues newEntry = new ContentValues();
            newEntry.put(AnalyticsOpenHelper.TYPE_FIELD, eventType);
            newEntry.put(AnalyticsOpenHelper.DATE_FIELD, getCurrentTimestamp());
            newEntry.put(AnalyticsOpenHelper.EXTRA_FIELD, extraString);

            // Try to add the event
            database.beginTransaction();
            try {
                database.insertOrThrow(AnalyticsOpenHelper.TABLE_NAME, null, newEntry);
                database.setTransactionSuccessful();
                LOGV(TAG, "Event queued");
            } catch (SQLiteException sqle) {
                LOGE(TAG, "Failed to queue event: " + sqle.getMessage());
            } finally {
                database.endTransaction();
            }

            // Close database
            database.close();
        }
    }

    /**
     * Attempt to remove & send events from the SQLite database
     * @param workIntent Work intent given to service
     */
    private void doPost(Intent workIntent) {
        JSONArray eventOutQueue = new JSONArray();

        LOGI(TAG, "Attempting to post events");

        // Open the event database
        AnalyticsOpenHelper analyticsOpenHelper = new AnalyticsOpenHelper(this);
        SQLiteDatabase database;
        try {
            database = analyticsOpenHelper.getWritableDatabase();
        } catch (SQLiteException sqle) {
            LOGE(TAG, sqle.getMessage());
            return;
        }

        JSONObject platform = getPlatformJSON(this);
        JSONObject release = getReleaseJSON(this);

        // Read out events and construct POST request
        database.beginTransaction();
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + AnalyticsOpenHelper.TABLE_NAME, null);

            if (cursor.getCount() == 0) {
                LOGI(TAG, "No events to post.");
            } else {
                while (cursor.moveToNext()) {
                    String type = cursor.getString(1);
                    String time = cursor.getString(2);
                    String extra = cursor.getString(3);

                    // Get JSON object of analytics event
                    JSONObject eventJSON = getEventJSON(this, type, time, platform, release);

                    // Load extra fields
                    if (extra != null) {
                        try {
                            JSONObject extraJSON = new JSONObject(extra);
                            for (Iterator<String> keys = extraJSON.keys(); keys.hasNext();) {
                                String curKey = keys.next();
                                eventJSON.put(curKey, extraJSON.get(curKey));
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, Log.getStackTraceString(e));
                        }
                    }

                    // Add to the JSON array for posting
                    eventOutQueue.put(eventJSON);
                }

                // Delete loaded rows from database
                database.delete(AnalyticsOpenHelper.TABLE_NAME, null, null);

                try {
                    // Build POST request
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, eventOutQueue.toString());
                    Request request = new Request.Builder()
                            .url(POST_URL)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if(response.code() >= 200 && response.code() <= 299) {
                        database.setTransactionSuccessful();
                        LOGI(TAG, cursor.getCount() + " events posted.");
                    }
                } catch (IOException e) {
                    LOGE(TAG, e.getMessage());
                }
            }

            cursor.close();
        } catch (SQLiteException sqle) {
            LOGE(TAG, "Failed to post events: " + sqle.getMessage());
        } finally {
            database.endTransaction();
        }

        // Close database
        database.close();
    }

    /**
     * Get a string representation of the time that can be parsed by PHP's strtotime() function.
     * @return String representation of the current time which can be parsed by strtotime()
     */
    public static String getCurrentTimestamp() {
        return "@" + Long.toString(System.currentTimeMillis()/1000L);
    }

    /**
     * Get JSON object describing an analytics event.
     * @param eventType Event type
     * @param timestamp Timestamp for when the event occurred
     * @param context App context
     * @return JSON object describing the analytics event.
     */
    private static JSONObject getEventJSON(Context context, String eventType, String timestamp,
                                           JSONObject platform, JSONObject release) {
        JSONObject eventJSON = new JSONObject();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userCampus = RutgersUtils.getFullCampusTitle(context, prefs.getString(PrefUtils.KEY_PREF_HOME_CAMPUS, null));
        String userRole = RutgersUtils.getFullRoleTitle(context, prefs.getString(PrefUtils.KEY_PREF_USER_TYPE, null));

        try {
            eventJSON.put("type", eventType);
            eventJSON.put("role", userRole);
            eventJSON.put("campus", userCampus);
            eventJSON.put("date", timestamp);
            eventJSON.put("platform", platform);
            eventJSON.put("release", release);
        } catch (JSONException e) {
            LOGW(TAG, "getJSON(): " + e.getMessage());
        }

        return eventJSON;
    }

    /**
     * Get JSON object describing the current device.
     * @param context App context
     * @return JSON object describing the current device
     */
    private static JSONObject getPlatformJSON(Context context) {
        JSONObject platformJSON = new JSONObject();
        try {
            platformJSON.put("os", Config.OSNAME);
            platformJSON.put("version", Build.VERSION.RELEASE);
            platformJSON.put("model", Build.MANUFACTURER + " " + Build.MODEL);
            platformJSON.put("tablet", AppUtils.isTablet(context));
            platformJSON.put("android", Build.VERSION.SDK_INT);
            platformJSON.put("id", AppUtils.getUUID(context));
        } catch (JSONException e) {
            LOGW(TAG, "getPlatformJSON(): " + e.getMessage());
        }
        return platformJSON;
    }

    /**
     * Get JSON object describing the current app release.
     * @param context App context
     * @return JSON object describing the current release
     */
    private static JSONObject getReleaseJSON(Context context) {
        JSONObject releaseJSON = new JSONObject();
        try {
            releaseJSON.put("debug", BuildConfig.DEBUG);
            releaseJSON.put("beta", Config.BETA);
            releaseJSON.put("version", Config.VERSION);
            releaseJSON.put("api", Config.API_LEVEL);
        } catch (JSONException e) {
            LOGW(TAG, "getReleaseJSON(): " + e.getMessage());
        }
        return releaseJSON;
    }

}
