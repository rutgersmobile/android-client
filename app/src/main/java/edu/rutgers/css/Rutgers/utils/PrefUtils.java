package edu.rutgers.css.Rutgers.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * Preference keys and helpers.
 */
public final class PrefUtils {
    private PrefUtils() {}

    /** Key for string containing the user's home campus tag. (nb, nwk, cam, etc.) */
    public static final String KEY_PREF_HOME_CAMPUS     = "pref_home_campus";

    /** Key for string containing the user's role tag (undergrad, grad, alumni, etc.) */
    public static final String KEY_PREF_USER_TYPE       = "pref_user_type";

    /** Key for string containing the saved SOC API campus tag. */
    public static final String KEY_PREF_SOC_CAMPUS      = "pref_soc_campus";

    /** Key for string containing the saved SOC API level tag. */
    public static final String KEY_PREF_SOC_LEVEL       = "pref_soc_level";

    /** Key for string containing the saved SOC API semester tag. */
    public static final String KEY_PREF_SOC_SEMESTER    = "pref_soc_semester";

    /** Key for boolean "first launch" flag. */
    public static final String KEY_PREFS_FIRST_LAUNCH   = "pref_first_launch";

    public static boolean isFirstLaunch(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_PREFS_FIRST_LAUNCH, true);
    }

    public static void markFirstLaunch(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_PREFS_FIRST_LAUNCH, false).apply();
    }

}
