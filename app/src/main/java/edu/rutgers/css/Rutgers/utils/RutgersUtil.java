package edu.rutgers.css.Rutgers.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers2.R;
import edu.rutgers.css.Rutgers2.SettingsActivity;

/**
 * Utilities for Rutgers-specific data
 */
public class RutgersUtil {

    private static final String TAG = "RutgersUtil";

    /**
     * Get full campus title from campus tag.
     * @param context App context
     * @param campusTag Campus tag
     * @return Full campus title
     */
    public static String getFullCampusTitle(@NonNull Context context, String campusTag) {
        if(campusTag == null) return null;

        Resources res = context.getResources();
        try {
            int id = res.getIdentifier("campus_"+campusTag+"_full", "string", Config.PACKAGE_NAME);
            return res.getString(id);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /**
    * Get full role title from role tag.
    * @param context App context
    * @param roleTag Role tag
    * @return Full role title
    */
    public static String getFullRoleTitle(@NonNull Context context, String roleTag) {
        if(roleTag == null) return null;

        Resources res = context.getResources();
        try {
            int id = res.getIdentifier("role_"+roleTag+"_full", "string", Config.PACKAGE_NAME);
            return res.getString(id);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /**
     * In cases where multiple titles are specified ("homeTitle", "foreignTitle"), gets appropriate title
     * according to user configuration.
     * @param title String or JSONObject returned by get("title") on channel JSONObject
     * @return Appropriate title to display
     */
    @NonNull
    public static String getLocalTitle(@NonNull Context context, Object title) {
        if(title == null) {
            return "(No title)";
        } else if(title.getClass() == String.class) {
            // "title" is just a string - nothing to do
            return (String) title;
        } else if(title.getClass() == JSONObject.class) {
            // "title" is a JSON Object - figure out which string to display
            JSONObject titles = (JSONObject) title;
            String userHome = getHomeCampus(context);

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
        } else {
            Log.e(TAG, "Unexpected class for title: " + title.getClass().getSimpleName());
            return "(Invalid title)";
        }
    }

    /**
    * Get the full name of the user's home campus (defaults to New Brunswick if no prefs are set)
    * @param context App context
    * @return Full title of user's home campus
    */
    public static String getHomeCampus(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getFullCampusTitle(context, sharedPref.getString(SettingsActivity.KEY_PREF_HOME_CAMPUS, context.getString(R.string.campus_nb_tag)));
    }

}
