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
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.rutgers.css.Rutgers.items.AnalyticsOpenHelper;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers.utils.RutgersUtil;
import edu.rutgers.css.Rutgers2.BuildConfig;
import edu.rutgers.css.Rutgers2.SettingsActivity;

/**
 * Analytics service. Queues analytics events and flushes them to the server when the app is paused.
 * <p>
 * The events are stored in a local SQLite database until they are successfully flushed to the server.
 * @author James Chambers
 */
public class Analytics extends IntentService {

    public static final String TAG = "Analytics";

    private static final String POST_URL = "http://sauron.rutgers.edu/~jamchamb/analytics.php"; // TODO Remove
    //private static final String POST_URL = AppUtil.API_BASE + "analytics.php";

    // Event types
    public static final String NEW_INSTALL = "fresh_launch";
    public static final String LAUNCH = "launch";
    public static final String ERROR = "error";
    public static final String CHANNEL_OPENED = "channel";
    public static final String DEFAULT_TYPE = "event";

    private static final int QUEUE_MODE = 0;
    private static final int POST_MODE = 1;

    public Analytics() {
        super("Analytics");
    }

    /**
     * Queue an analytics event.
     * @param context App context
     * @param eventType Event type
     * @param extra Extra information, in a string
     */
    public static void queueEvent(Context context, String eventType, String extra) {
        Intent analyticsIntent = new Intent(context, Analytics.class);
        analyticsIntent.putExtra("mode", QUEUE_MODE);
        analyticsIntent.putExtra("type", eventType);
        analyticsIntent.putExtra("extra", extra);
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
                Log.e(TAG, "Invalid mode supplied");
        }

    }

    /**
     * Queue the event in the SQLite database.
     * @param workIntent Work intent given to service
     */
    private void doQueue(Intent workIntent) {
        String eventType = workIntent.getStringExtra("type");
        String extraString = workIntent.getStringExtra("extra");

        if(eventType != null) {
            Log.v(TAG, "Queueing " + eventType + " event");

            // Open the event database
            AnalyticsOpenHelper analyticsOpenHelper = new AnalyticsOpenHelper(this);
            SQLiteDatabase database;
            try {
                database = analyticsOpenHelper.getWritableDatabase();
            } catch (SQLiteException sqle) {
                Log.e(TAG, sqle.getMessage());
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
                Log.v(TAG, "Event queued");
            } catch (SQLiteException sqle) {
                Log.e(TAG, "Failed to queue event: " + sqle.getMessage());
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

        Log.i(TAG, "Attempting to post events");

        // Open the event database
        AnalyticsOpenHelper analyticsOpenHelper = new AnalyticsOpenHelper(this);
        SQLiteDatabase database;
        try {
            database = analyticsOpenHelper.getWritableDatabase();
        } catch (SQLiteException sqle) {
            Log.e(TAG, sqle.getMessage());
            return;
        }

        // Read out events and construct POST request
        database.beginTransaction();
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + AnalyticsOpenHelper.TABLE_NAME, null);

            if(cursor.getCount() == 0) {
                Log.i(TAG, "No events to post.");
            }
            else {
                while (cursor.moveToNext()) {
                    String type = cursor.getString(1);
                    String time = cursor.getString(2);
                    String extra = cursor.getString(3);

                    // Get JSON object of analytics event
                    JSONObject eventJSON = getEventJSON(type, time, this);

                    // Load extra fields
                    if(extra != null) {
                        try {
                            JSONObject extraJSON = new JSONObject(extra);
                            Iterator<String> keys = extraJSON.keys();
                            while (keys.hasNext()) {
                                String curKey = keys.next();
                                eventJSON.put(curKey, extraJSON.get(curKey));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "doPost(): " + e.getMessage());
                        }
                    }

                    // Add to the JSON array for posting
                    eventOutQueue.put(eventJSON);
                }

                // Delete loaded rows from database
                database.delete(AnalyticsOpenHelper.TABLE_NAME, null, null);

                // Build POST request
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(POST_URL);

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("payload", eventOutQueue.toString()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    // Successful POST - commit transaction to remove rows
                    Log.i(TAG, httpResponse.getStatusLine().toString());
                    int responseCode = httpResponse.getStatusLine().getStatusCode();
                    if(responseCode >= 200 && responseCode <= 299) {
                        database.setTransactionSuccessful();
                        Log.i(TAG, cursor.getCount() + " events posted.");
                    }
                } catch (ClientProtocolException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

            }

        } catch (SQLiteException sqle) {
            Log.e(TAG, "Failed to post events: " + sqle.getMessage());
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
        return "@"+new Long(System.currentTimeMillis()/1000L).toString();
    }

    /**
     * Get JSON object describing an analytics event.
     * @param eventType Event type
     * @param timestamp Timestamp for when the event occurred
     * @param context App context
     * @return JSON object describing the analytics event.
     */
    private static JSONObject getEventJSON(String eventType, String timestamp, Context context) {
        JSONObject eventJSON = new JSONObject();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userCampus = RutgersUtil.getFullCampusTitle(context, prefs.getString(SettingsActivity.KEY_PREF_HOME_CAMPUS, null));
        String userRole = RutgersUtil.getFullRoleTitle(context, prefs.getString(SettingsActivity.KEY_PREF_USER_TYPE, null));

        try {
            eventJSON.put("type", eventType);
            eventJSON.put("role", userRole);
            eventJSON.put("campus", userCampus);
            eventJSON.put("date", timestamp);
            eventJSON.put("platform", getPlatformJSON(context));
            eventJSON.put("release", getReleaseJSON(context));
        } catch (JSONException e) {
            Log.w(TAG, "getJSON(): " + e.getMessage());
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

    /**
     * Get JSON object describing the current app release.
     * @param context App context
     * @return JSON object describing the current release
     */
    private static JSONObject getReleaseJSON(Context context) {
        JSONObject releaseJSON = new JSONObject();
        try {
            releaseJSON.put("debug", BuildConfig.DEBUG);
            releaseJSON.put("beta", AppUtil.BETA);
            releaseJSON.put("version", AppUtil.VERSION);
            releaseJSON.put("api", AppUtil.API_LEVEL);
        } catch (JSONException e) {
            Log.w(TAG, "getReleaseJSON(): " + e.getMessage());
        }
        return releaseJSON;
    }

}
