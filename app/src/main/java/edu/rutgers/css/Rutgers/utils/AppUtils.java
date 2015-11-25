package edu.rutgers.css.Rutgers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.UUID;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.model.Channel;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

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
        if (installID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    FileOutputStream out = new FileOutputStream(installation);

                    // Either recover UUID from Titanium app or generate a new one
                    String uuid = recoverTitaniumID(context);
                    if (uuid == null) uuid = UUID.randomUUID().toString();

                    out.write(uuid.getBytes());
                    out.close();
                }
                installID = readInstallationFile(installation);
            } catch (Exception e) {
                LOGE(TAG, Log.getStackTraceString(e));
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
     * Attempt to read unique ID from Titanium database, if one is leftover from an older RUMobile
     * installation.
     * @return UUID as String if successful, null if not found.
     */
    private static String recoverTitaniumID(@NonNull Context context) {
        String result = null;

        File titaniumDB = context.getDatabasePath("Titanium");
        if (titaniumDB.exists()) {
            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(titaniumDB.getPath(), null, 0);

                // SELECT value FROM platform WHERE name = 'unique_machine_id' LIMIT 1
                Cursor dbCursor = db.query("platform", new String[]{"value"}, "name = 'unique_machine_id'",
                        null, null, null, null, "1");

                if (dbCursor.getCount() > 0) {
                    dbCursor.moveToFirst();
                    result = dbCursor.getString(dbCursor.getColumnIndex("value"));
                    LOGI(TAG, "Recovered Titanium UUID: " + result);
                } else {
                    LOGI(TAG, "Zero results");
                }

                db.close();
            } catch (SQLiteException e) {
                LOGW(TAG, e.getMessage());
            }
        } else {
            LOGI(TAG, "No Titanium database found.");
        }

        return result;
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

    /** Close soft keyboard */
    public static void closeKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /** Open soft keyboard */
    public static void openKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            imm.toggleSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    /**
     * Show a pop-up message saying that the attempt to get data has failed.
     * @param context App's context (activity)
     */
    public static void showFailedLoadToast(Context context) {
        if (context != null) Toast.makeText(context, R.string.failed_load, Toast.LENGTH_SHORT).show();
        else LOGW(TAG, "showFailedLoadToast(): context null");
    }

    /**
     * Check if the fragment on top of the stack has the given tag.
     * @param handle Fragment handle
     * @return True if on top, false if not
     */
    public static boolean isOnTop(FragmentActivity activity, @NonNull String component) {
        if (activity == null) return false;

        FragmentManager fm = activity.getSupportFragmentManager();
        return fm.getBackStackEntryCount() > 0 && component.equalsIgnoreCase(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
    }

    /**
     * Check if the fragment on top of the stack is the given component
     * @param activity The activity that the fragment is in
     * @param channelManager Location to get Channel info for determining the component of a fragment
     * @param handle The component to look for
     * @return True if on top, false if not
     */
    public static boolean isComponentOnTop(FragmentActivity activity, ChannelManager channelManager, @NonNull String handle, String lastFragmentTag) {
        if (activity == null) return false;

        FragmentManager fm = activity.getSupportFragmentManager();
        Channel channel = null;
        if (fm.getBackStackEntryCount() > 0) {
            channel = channelManager.getChannelByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
        } else if (lastFragmentTag != null){
            channel = channelManager.getChannelByTag(lastFragmentTag);
        }
        if (channel != null) {
            return handle.equalsIgnoreCase(channel.getView());
        }
        return false;
    }

    public static String topHandle(FragmentActivity activity) {
        if (activity == null) return null;

        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
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
            LOGE(TAG, e.getMessage());
            return null;
        }

        String contentString = stringBuilder.toString();
        if (contentString.isEmpty()) return null;
        else return contentString;
    }

    /**
     * Load JSON array from raw resource file
     * @param resources Application Resources
     * @param resourceId Raw resource file ID
     * @return JSON array or null if there was a problem loading the raw resource file
     */
    public static JsonArray loadRawJSONArray(@NonNull Resources resources, int resourceId) {
        String jsonString = loadRawResource(resources, resourceId);
        if (jsonString == null) return null;

        try {
            return new Gson().fromJson(jsonString, JsonArray.class);
        } catch (JsonSyntaxException e) {
            LOGE(TAG, "loadRawJSONArray(): " + e.getMessage());
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
        if (jsonString == null) return null;

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            LOGE(TAG, "loadRawJSONObject(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Remove HTML tags from string
     * @param string String to cleanse
     * @return Cleansed string, or null if string was null
     */
    public static String stripTags(String string) {
        if (string == null) return null;
        return string.replaceAll("\\<.*?\\>", "");
    }

}
