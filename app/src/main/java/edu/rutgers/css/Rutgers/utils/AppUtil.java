package edu.rutgers.css.Rutgers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
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

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

/**
 * General helper methods & global variables for the app
 */
public class AppUtil {

    private static final String TAG = "AppUtil";

    private static final String INSTALLATION = "INSTALLATION";
    private static String installID = null;

    public static final String APPTAG = "Rutgers";
    public static final String PACKAGE_NAME = "edu.rutgers.css.Rutgers2";
    public static final String VERSION = "4.0";
    public static final String OSNAME = "android";
    public static final String BETAMODE = "dev";
    public static final Boolean BETA = true;

    public static final String API_LEVEL = "1";
    public static final String API_BASE = "https://rumobile.rutgers.edu/"+API_LEVEL+"/";

    public static final float NEARBY_RANGE = 300.0f; // Within 300 meters is considered "nearby"

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
     * Get icon by resource ID, colored white
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @return Icon drawable
     */
    public static Drawable getIcon(@NonNull Resources resources, int drawableResource) {
        return getIcon(resources, drawableResource, R.color.white);
    }

    /**
     * Get icon by resource ID with specified color
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @param colorResource Color to be applied to icon
     * @return Icon drawable
     */
    public static Drawable getIcon(@NonNull Resources resources, int drawableResource, int colorResource) {
        if(drawableResource == 0) return null;
        if(colorResource == 0) colorResource = R.color.white;

        try {
            Drawable drawable = resources.getDrawable(drawableResource);
            int color = resources.getColor(colorResource);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return drawable;
        } catch(NotFoundException e) {
            Log.w(TAG, "getIcon(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Get icon by channel handle
     * @param resources Application Resources
     * @param handle Channel handle
     * @return Icon drawable for channel
     */
    public static Drawable getIcon(@NonNull Resources resources, String handle) {
        int iconRes = 0, colorRes = 0;

        // Look up the icon resource
        try {
            iconRes = resources.getIdentifier("ic_"+handle, "drawable", AppUtil.PACKAGE_NAME);
        } catch(NotFoundException e) {
            Log.i(TAG, "getIcon(): " + e.getMessage());
        }

        // Look up the color resource
        try {
            colorRes = resources.getIdentifier(handle+"_icon_color", "color", AppUtil.PACKAGE_NAME);
        } catch(NotFoundException e) {
            Log.i(TAG, "getIcon(): " + e.getMessage());
        }

        return getIcon(resources, iconRes, colorRes);
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
    public static boolean isOnTop(@NonNull String handle) {
        return handle.equalsIgnoreCase(ComponentFactory.getInstance().getTopHandle());
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

    public static Bitmap decodeSampledBitmapFromResource(@NonNull Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
