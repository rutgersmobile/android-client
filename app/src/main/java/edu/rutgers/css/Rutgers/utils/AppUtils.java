package edu.rutgers.css.Rutgers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import edu.rutgers.css.Rutgers.R;

/**
 * General helper methods & global variables for the app
 */
public final class AppUtils {

    private static final String TAG = "AppUtils";

    private static final String INSTALLATION = "INSTALLATION";
    private static String installID = null;

    private AppUtils() {}

    /**
     * Get (or create) UUID for the installation of this app.
     * @param context App context
     * @return UUID string
     */
    public synchronized static String getUUID(@NonNull Context context) {
        if(installID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if(!installation.exists()) {
                    FileOutputStream out = new FileOutputStream(installation);
                    out.write(UUID.randomUUID().toString().getBytes());
                    out.close();
                }
                installID = readInstallationFile(installation);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                throw new RuntimeException(e);
            }
        }
        return installID;
    }

    /**
     * Read the UUID file
     * @param installation Installation file
     * @return Contents of file in string
     * @throws IOException
     */
    private static String readInstallationFile(@NonNull File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    /**
     * Determine if the current device is a tablet.
     * @param context App context
     * @return True if the running device is a tablet, false if not.
     */
    public static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Close soft keyboard
     * @param activity App activity
     */
    public static void closeKeyboard(Activity activity) {
        if(activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Create custom string from Ajax Status object.
     * @param status AjaxStatus object
     * @return Custom string describing status
     */
    public static String formatAjaxStatus(@NonNull AjaxStatus status) {
        String translateStatusCode;
        switch(status.getCode()) {
            case AjaxStatus.NETWORK_ERROR:
                translateStatusCode = "NETWORK_ERROR";
                break;
            case AjaxStatus.AUTH_ERROR:
                translateStatusCode = "AUTH_ERROR";
                break;
            case AjaxStatus.TRANSFORM_ERROR:
                translateStatusCode = "TRANSFORM_ERROR";
                break;
            default:
                translateStatusCode = "other";
        }

        return "AJAX Response: " + status.getMessage() + " (" + status.getCode() + ": " + translateStatusCode + ")";
    }

    /**
     * Show a pop-up message saying that the attempt to get data has failed.
     * @param context App's context (activity)
     */
    public static void showFailedLoadToast(Context context) {
        if(context != null) Toast.makeText(context, R.string.failed_load, Toast.LENGTH_SHORT).show();
        else Log.w(TAG, "showFailedLoadToast(): context null");
    }

    /**
     * Check if the fragment on top of the stack has the given tag.
     * @param handle Fragment handle
     * @return True if on top, false if not
     */
    public static boolean isOnTop(FragmentActivity activity, @NonNull String handle) {
        if(activity == null) return false;

        FragmentManager fm = activity.getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0) {
            return handle.equalsIgnoreCase(fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName());
        } else {
            return false;
        }
    }

    public static String topHandle(FragmentActivity activity) {
        if(activity == null) return null;

        FragmentManager fm = activity.getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0) {
            return fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName();
        } else {
            return null;
        }
    }

    /**
     * Load string contents from raw resource file
     * @param resources Application Resources
     * @param resourceId Raw resource file ID
     * @return Contents of resource file as a string, or null if file was empty or couldn't be read.
     */
    public static String loadRawResource(@NonNull Resources resources, int resourceId) {
        InputStream is = resources.openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();

        try {
            String readLine;
            while((readLine = br.readLine()) != null) {
                stringBuilder.append(readLine);
            }
            is.close();
            br.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

        String contentString = stringBuilder.toString();
        if(contentString.isEmpty()) return null;
        else return contentString;
    }

    /**
     * Load JSON array from raw resource file
     * @param resources Application Resources
     * @param resourceId Raw resource file ID
     * @return JSON array or null if there was a problem loading the raw resource file
     */
    public static JSONArray loadRawJSONArray(@NonNull Resources resources, int resourceId) {
        String jsonString = loadRawResource(resources, resourceId);
        if(jsonString == null) return null;

        try {
            return new JSONArray(jsonString);
        } catch(JSONException e) {
            Log.e(TAG, "loadRawJSONArray(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Load JSON object from raw resource file
     * @param resources Application Resources
     * @param resourceId Raw resource file ID
     * @return JSON object or null if there was a problem loading the raw resource file
     */
    public static JSONObject loadRawJSONObject(@NonNull Resources resources, int resourceId) {
        String jsonString = loadRawResource(resources, resourceId);
        if(jsonString == null) return null;

        try {
            return new JSONObject(jsonString);
        } catch(JSONException e) {
            Log.e(TAG, "loadRawJSONObject(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if two different timestamps have the same date.
     * @param d1 Date 1
     * @param d2 Date 2
     * @return True if days of year match, false if not
     */
    public static boolean isSameDay(@NonNull Date d1, @NonNull Date d2) {
        Calendar cal1 = Calendar.getInstance(Locale.US);
        Calendar cal2 = Calendar.getInstance(Locale.US);
        cal1.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal2.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Remove HTML tags from string
     * @param string String to cleanse
     * @return Cleansed string, or null if string was null
     */
    public static String stripTags(String string) {
        if(string == null) return null;
        return string.replaceAll("\\<.*?\\>", "");
    }

}
