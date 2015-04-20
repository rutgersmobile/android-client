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
    public static final String KEY_PREF_FIRST_LAUNCH = "pref_first_launch";

    /** Key for boolean "first drawer open" flag. */
    public static final String KEY_PREF_FIRST_DRAWER    = "pref_first_drawer";

    /** Key for tutorial stage. */
    public static final String KEY_PREF_TUTORIAL_STAGE = "pref_tut_stage";

    public static boolean isFirstLaunch(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_PREF_FIRST_LAUNCH, true);
    }

    public static void markFirstLaunch(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_PREF_FIRST_LAUNCH, false).apply();
    }

    /** True if the user has opened the navigation at least once (for tutorial purposes). */
    public static boolean hasDrawerBeenUsed(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_PREF_FIRST_DRAWER, false);
    }

    /** Call when the navigation drawer has been opened to indicate that the
     * user has opened the navigation drawer at least once (for tutorial purposes). */
    public static void markDrawerUsed(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_PREF_FIRST_DRAWER, true).apply();
    }

    public static int getTutorialStage(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_PREF_TUTORIAL_STAGE, 0);
    }

    public static void advanceTutorialStage(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(KEY_PREF_TUTORIAL_STAGE, prefs.getInt(KEY_PREF_TUTORIAL_STAGE, 0)+1).apply();
    }

    public static void setTutorialStage(@NonNull Context context, int stage) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(KEY_PREF_TUTORIAL_STAGE, stage).apply();
    }

}
