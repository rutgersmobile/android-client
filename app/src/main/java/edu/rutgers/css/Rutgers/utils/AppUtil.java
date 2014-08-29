package edu.rutgers.css.Rutgers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
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
import java.util.UUID;

import edu.rutgers.css.Rutgers2.SettingsActivity;
import edu.rutgers.css.Rutgers2.R;

public class AppUtil {

	private static final String TAG = "AppUtil";
	private static final String INSTALLATION = "INSTALLATION";
	private static String installID = null;

    public static final String APPTAG = "Rutgers";
    public static final String PACKAGE_NAME = "edu.rutgers.css.Rutgers2";
    public static final String VERSION = "0.0";
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
	public synchronized static String getUUID(Context context) {
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
	private static String readInstallationFile(File installation) throws IOException {
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
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

	/**
	 * Get full campus title from campus tag.
	 * @param context App context
	 * @param campusTag Campus tag
	 * @return Full campus title
	 */
	public static String getFullCampusTitle(Context context, String campusTag) {
		if(campusTag == null) return null;
		
		Resources res = context.getResources();
		try {
			int id = res.getIdentifier("campus_"+campusTag+"_full", "string", AppUtil.PACKAGE_NAME);
			return res.getString(id);
		} catch (NotFoundException e) {
			return null;
		}
	}

    /**
     * Get full role title from role tag.
     * @param context App context
     * @param roleTag Role tag
     * @return Full role title
     */
    public static String getFullRoleTitle(Context context, String roleTag) {
        if(roleTag == null) return null;

        Resources res = context.getResources();
        try {
            int id = res.getIdentifier("role_"+roleTag+"_full", "string", AppUtil.PACKAGE_NAME);
            return res.getString(id);
        } catch (NotFoundException e) {
            return null;
        }
    }

	/**
	 * In cases where multiple titles are specified ("homeTitle", "foreignTitle"), gets appropriate title
	 * according to user configuration.
	 * @param title String or JSONObject returned by get("title") on channel JSONObject
	 * @return Appropriate title to display
	 */
	public static String getLocalTitle(Context context, Object title) {
		if(title == null) {
			return "(No title)";
		}
		// "title" is just a string - nothing to do
		else if(title.getClass() == String.class) {
			return (String) title;
		}
		// "title" is a JSON Object - figure out which string to display
		else if(title.getClass() == JSONObject.class) {
			JSONObject titles = (JSONObject) title;
			
			// Get the full name of the user's home campus (default to NB if there's no config)
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String userHome = AppUtil.getFullCampusTitle(context, sharedPref.getString(SettingsActivity.KEY_PREF_HOME_CAMPUS, context.getResources().getString(R.string.campus_nb_tag)));
			//Log.v(TAG, "User home: " + userHome);
			
			try {
				String titleHome = titles.getString("homeCampus");
				// If user config home matches title's home, show home title
				if(titleHome.equalsIgnoreCase(userHome)) {
					return titles.getString("homeTitle");
				}
				// If not, use foreign title
				else {
					return titles.getString("foreignTitle");
				}
			} catch (JSONException e) {
				Log.w(TAG, "getLocalTitle(): " + e.getMessage());
				Log.w(TAG, "title JSON: " + title.toString());
				return title.toString();
			}
		}
		
		return null;
	}

    /**
     * Get icon by resource ID, colored white
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @return Icon drawable
     */
    public static Drawable getIcon(Resources resources, int drawableResource) {
        return getIcon(resources, drawableResource, R.color.white);
    }

    /**
     * Get icon by resource ID with specified color
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @param colorResource Color to be applied to icon
     * @return Icon drawable
     */
    public static Drawable getIcon(Resources resources, int drawableResource, int colorResource) {
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
    public static Drawable getIcon(Resources resources, String handle) {
        int iconRes = 0;
        int colorRes = 0;
        try {
            iconRes = resources.getIdentifier(handle, "drawable", AppUtil.PACKAGE_NAME);
        } catch(NotFoundException e) {
            Log.i(TAG, "getIcon(): " + e.getMessage());
        }
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
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Create custom string from Ajax Status object.
     * @param status AjaxStatus object
     * @return Custom string describing status
     */
    public static String formatAjaxStatus(AjaxStatus status) {
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
     * Get the tag for the fragment on top of the stack.
     * @param fragmentManager App's {@link android.support.v4.app.FragmentManager}
     * @return Name that was supplied to {@link android.support.v4.app.FragmentManager #addToBackStack(java.lang.String)}.
     */
    public static String backStackPeek(FragmentManager fragmentManager) {
        if(fragmentManager == null || fragmentManager.getBackStackEntryCount() == 0) {
            if(fragmentManager == null) Log.v(TAG, "Null fragment manager");
            else Log.v(TAG, "Backstack empty");
            return null;
        }

        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-1);
        Log.v(TAG, "Top of backstack: " + backStackEntry.getName());
        return backStackEntry.getName();
    }

    /**
     * Check if the fragment on top of the stack has the given tag.
     * @param handle Fragment handle
     * @param fragmentManager App's {@link android.support.v4.app.FragmentManager}
     * @return True if on top, false if not
     */
    public static boolean isOnTop(String handle, FragmentManager fragmentManager) {
        if(handle == null) return false;
        return handle.equalsIgnoreCase(backStackPeek(fragmentManager));
    }

    /**
     * Load string contents from raw resource file
     * @param resources Application Resources
     * @param resourceId Raw resource file ID
     * @return Contents of resource file as a string, or null if file was empty or couldn't be read.
     */
    public static String loadRawResource(Resources resources, int resourceId) {
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
    public static JSONArray loadRawJSONArray(Resources resources, int resourceId) {
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
    public static JSONObject loadRawJSONObject(Resources resources, int resourceId) {
        String jsonString = loadRawResource(resources, resourceId);
        if(jsonString == null) return null;

        try {
            return new JSONObject(jsonString);
        } catch(JSONException e) {
            Log.e(TAG, "loadRawJSONObject(): " + e.getMessage());
            return null;
        }
    }

}
