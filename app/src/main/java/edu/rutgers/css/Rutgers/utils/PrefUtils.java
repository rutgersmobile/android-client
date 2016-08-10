package edu.rutgers.css.Rutgers.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import edu.rutgers.css.Rutgers.link.Link;

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

    public static final String KEY_PREF_BOOKMARK        = "pref_bookmark";

    public static final String KEY_PREF_GPS_REQUEST = "pref_gps_request";

    public static String getHomeCampus(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_PREF_HOME_CAMPUS, "NB");
    }

    public static void setGPSRequest(@NonNull Context context, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_PREF_GPS_REQUEST, b).apply();
    }

    public static boolean getGPSRequest(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_PREF_GPS_REQUEST, true);
    }

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

    public static List<Link> getBookmarks(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Link>>(){}.getType();
        String emptyList = gson.toJson(new ArrayList<Link>(), type);
        String prefString = prefs.getString(KEY_PREF_BOOKMARK, emptyList);
        List<Link> links = gson.fromJson(prefString, type);
        return dedupLinks(links);
    }

    public static void setBookmarksFromUpstream(@NonNull Context context, List<Link> links) {
        links = mergeLinks(links, getBookmarks(context));
        setBookmarks(context, links);
    }

    public static void setBookmarks(@NonNull Context context, List<Link> links) {
        links = dedupLinks(links);
        links = arrangeLinks(links);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Link>>(){}.getType();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(KEY_PREF_BOOKMARK, gson.toJson(links, type)).apply();
    }

    public static void addBookmark(@NonNull Context context, int location, @NonNull Link link) {
        List<Link> links = getBookmarks(context);
        links.add(location, link);
        setBookmarks(context, links);
    }

    public static void addBookmark(@NonNull Context context, @NonNull Link link) {
        List<Link> links = getBookmarks(context);
        links.add(link);
        setBookmarks(context, links);
    }

    public static void removeBookmark(@NonNull Context context, int position) {
        List<Link> links = getBookmarks(context);
        if (position < links.size()) {
            links.remove(position);
        }
        setBookmarks(context, links);
    }

    private static List<Link> mergeLinks(List<Link> upstream, List<Link> user) {
        final List<Link> mergedLinks = new ArrayList<>();
        for (final Link userLink : user) {
            for (final Link upstreamLink : upstream) {
                // If we can't find a link upstream then we should remove it
                // This means that the channel was deleted
                if (upstreamLink.getHandle().equals(userLink.getHandle())) {
                    mergedLinks.add(userLink);
                    break;
                }
            }
        }

        for (final Link upstreamLink : upstream) {
            boolean found = false;
            for (final Link userLink : user) {
                if (upstreamLink.getHandle().equals(userLink.getHandle())) {
                    found = true;
                    break;
                }
            }

            // Upstream channels not found should be added to the top
            if (!found) {
                mergedLinks.add(0, upstreamLink);
            }
        }

        return mergedLinks;
    }

    private static List<Link> dedupLinks(List<Link> links) {
        return new ArrayList<>(new LinkedHashSet<>(links));
    }

    private static List<Link> arrangeLinks(List<Link> links) {
        final List<Link> enabled = new ArrayList<>();
        final List<Link> disabled = new ArrayList<>();
        for (final Link link : links) {
            if (link.isEnabled()) {
                enabled.add(link);
            } else {
                disabled.add(link);
            }
        }
        enabled.addAll(disabled);
        return enabled;
    }

}
