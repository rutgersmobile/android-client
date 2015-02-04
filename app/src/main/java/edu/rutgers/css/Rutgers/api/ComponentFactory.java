package edu.rutgers.css.Rutgers.api;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusMain;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.channels.feedback.fragments.FeedbackMain;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMain;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesMain;
import edu.rutgers.css.Rutgers.channels.reader.fragments.RSSReader;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationDisplay;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationHoursDisplay;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationMain;
import edu.rutgers.css.Rutgers.channels.ruinfo.fragments.RUInfoMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCCourses;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCSections;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Singleton component builder
 */
public final class ComponentFactory {

    /** Log tag. */
    private static final String TAG = "ComponentFactory";

    /* Standard argument bundle tags */

    /** Handle of fragment to launch. */
    public static final String ARG_COMPONENT_TAG    = "component";

    /** Title to display in action bar. */
    public static final String ARG_TITLE_TAG        = "title";

    /** Channel handle, usually JSON-defined. */
    public static final String ARG_HANDLE_TAG       = "handle";

    /** JSON data passed as a string. */
    public static final String ARG_DATA_TAG         = "data";

    /** URL argument. */
    public static final String ARG_URL_TAG          = "url";

    /** RUMobile API file argument. */
    public static final String ARG_API_TAG          = "api";

    /** Count argument. For example, the number of events to grab in Events. */
    public static final String ARG_COUNT_TAG        = "count";

    /* Static data. */

    /** Component factory singleton. */
    private static ComponentFactory sSingletonInstance;

    /** Reference to the activity to switch fragments for. */
    private static WeakReference<FragmentActivity> sMainActivity;

    /** Table of fragments that can be launched. Handles must be lowercase. */
    private static Map<String, Class<? extends Fragment>> sFragmentTable =
            Collections.unmodifiableMap(new HashMap<String, Class<? extends Fragment>>() {{
        // General views
        put(DTable.HANDLE, DTable.class);
        put(RSSReader.HANDLE, RSSReader.class);
        put(TextDisplay.HANDLE, TextDisplay.class);
        put(WebDisplay.HANDLE, WebDisplay.class);

        // Bus views
        put(BusMain.HANDLE, BusMain.class);
        put(BusDisplay.HANDLE, BusDisplay.class);

        // Food views
        put(FoodMain.HANDLE, FoodMain.class);
        put(FoodHall.HANDLE, FoodHall.class);
        put(FoodMeal.HANDLE, FoodMeal.class);

        // Places views
        put(PlacesMain.HANDLE, PlacesMain.class);
        put(PlacesDisplay.HANDLE, PlacesDisplay.class);

        // Recreation views
        put(RecreationMain.HANDLE, RecreationMain.class);
        put(RecreationDisplay.HANDLE, RecreationDisplay.class);
        put(RecreationHoursDisplay.HANDLE, RecreationHoursDisplay.class);

        // SOC views
        put(SOCMain.HANDLE, SOCMain.class);
        put(SOCCourses.HANDLE, SOCCourses.class);
        put(SOCSections.HANDLE, SOCSections.class);

        // Other views
        put(RUInfoMain.HANDLE, RUInfoMain.class);
        put(FeedbackMain.HANDLE, FeedbackMain.class);
        put(AboutDisplay.HANDLE, AboutDisplay.class);
    }});

    /** Private constructor to enforce singleton usage. */
    private ComponentFactory() {}

    public void setMainActivity(@NonNull FragmentActivity fragmentActivity) {
        sMainActivity = new WeakReference<>(fragmentActivity);
    }

    /**
     * Get singleton instance.
     * @return Component factory singleton instance
     */
    public static ComponentFactory getInstance() {
        if (sSingletonInstance == null) sSingletonInstance = new ComponentFactory();
        return sSingletonInstance;
    }
    
    /**
     * Create component fragment
     * @param args Argument bundle with at least 'component' argument set to describe which
     *                component to build. The options bundle will be passed to the new fragment.
     * @return Built fragment
     */
    private Fragment createFragment(@NonNull Bundle args) {
        Fragment fragment;
        String component;
        
        if (StringUtils.isBlank(args.getString(ARG_COMPONENT_TAG))) {
            throw new IllegalArgumentException("\"" + ARG_COMPONENT_TAG + "\" must be set");
        }
        
        component = args.getString(ARG_COMPONENT_TAG).toLowerCase(Locale.US);
        Class<? extends Fragment> componentClass = sFragmentTable.get(component);
        if (componentClass != null) {
            Log.v(TAG, "Creating a " + componentClass.getSimpleName());
            try {
                fragment = componentClass.newInstance();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                return null;
            }    
        } else {
            Log.e(TAG, "Component \"" + component + "\" not found");
            return null;
        }

        Bundle argsCopy = new Bundle(args);
        argsCopy.remove(ARG_COMPONENT_TAG);
        fragment.setArguments(argsCopy);

        return fragment;
    }
    
    /**
     * Add current fragment to the backstack and switch to the new one defined by given arguments.
     * For calls from the nav drawer, this will attempt to pop all backstack history until the last 
     * time the desired channel was launched.
     * @param args Argument bundle with at least 'component' argument set to describe which
     *             component to build. All other arguments will be passed to the new fragment.
     * @return True if the new fragment was successfully created, false if not.
     */
    public boolean switchFragments(@NonNull Bundle args) {
        if (sMainActivity.get() == null) {
            Log.e(TAG, "switchFragments(): Reference to main activity is null");
            return false;
        }

        final String componentTag = args.getString(ARG_COMPONENT_TAG);
        final boolean isTopLevel = args.getBoolean("topLevel");

        // Attempt to create the fragment
        final Fragment fragment = createFragment(args);
        if (fragment == null) {
            sendChannelErrorEvent(args); // Channel launch failure analytics event
            return false;
        } else {
            sendChannelEvent(args); // Channel launch analytics event
        }

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtils.closeKeyboard(sMainActivity.get());

        FragmentManager fm = sMainActivity.get().getSupportFragmentManager();

        // If this is a top level (nav drawer) press, find the last time this channel was launched
        // and pop backstack to it
        if (isTopLevel && fm.findFragmentByTag(componentTag) != null) {
            fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
                
        // Switch the main content fragment
        fm.beginTransaction()
            .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left,
                    R.animator.slide_in_left, R.animator.slide_out_right)
            .replace(R.id.main_content_frame, fragment, componentTag)
            .addToBackStack(componentTag)
            .commit();

        return true;
    }

    /**
     * Show a dialog fragment and add it to backstack
     * @param dialogFragment Dialog fragment to display
     * @param tag Tag for fragment transaction backstack
     */
    public void showDialogFragment(@NonNull DialogFragment dialogFragment, @NonNull String tag) {
        if (sMainActivity.get() == null) {
            Log.e(TAG, "switchFragments(): Reference to main activity lost");
            return;
        }

        FragmentTransaction ft = sMainActivity.get().getSupportFragmentManager().beginTransaction();
        Fragment prev = sMainActivity.get().getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(tag);
        dialogFragment.show(ft, tag);
    }

    private void sendChannelEvent(@NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("handle", args.getString(ARG_COMPONENT_TAG));
            if(args.getString(ARG_URL_TAG) != null) extras.put("url", args.getString(ARG_URL_TAG));
            if(args.getString(ARG_API_TAG) != null) extras.put("api", args.getString(ARG_API_TAG));
            if(args.getString(ARG_TITLE_TAG) != null) extras.put("title", args.getString(ARG_TITLE_TAG));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Analytics.queueEvent(sMainActivity.get(), Analytics.CHANNEL_OPENED, extras);
    }

    private void sendChannelErrorEvent(@NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("description","failed to open channel");
            extras.put("handle", args.getString(ARG_COMPONENT_TAG));
            if(args.getString(ARG_URL_TAG) != null) extras.put("url", args.getString(ARG_URL_TAG));
            if(args.getString(ARG_API_TAG) != null) extras.put("api", args.getString(ARG_API_TAG));
            if(args.getString(ARG_TITLE_TAG) != null) extras.put("title", args.getString(ARG_TITLE_TAG));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Analytics.queueEvent(sMainActivity.get(), Analytics.ERROR, extras);
    }

}
