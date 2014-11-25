package edu.rutgers.css.Rutgers.api;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

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
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Singleton fragment builder
 */
public final class ComponentFactory {

    private static final String TAG = "ComponentFactory";

    /* Standard argument bundle tags */
    public static final String ARG_COMPONENT_TAG    = "component";
    public static final String ARG_TITLE_TAG        = "title";
    public static final String ARG_HANDLE_TAG       = "handle";
    public static final String ARG_DATA_TAG         = "data";
    public static final String ARG_URL_TAG          = "url";
    public static final String ARG_API_TAG          = "api";
    public static final String ARG_COUNT_TAG        = "count";

    private static ComponentFactory sSingletonInstance;
    private static WeakReference<FragmentActivity> sMainActivity;

    // Set up table of fragments that can be launched
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
    }});

    private ComponentFactory() {}

    public void setMainActivity(@NonNull FragmentActivity fragmentActivity) {
        sMainActivity = new WeakReference<>(fragmentActivity);
    }

    /**
     * Get singleton sSingletonInstance
     * @return Component factory singleton sSingletonInstance
     */
    public static ComponentFactory getInstance() {
        if(sSingletonInstance == null) sSingletonInstance = new ComponentFactory();
        return sSingletonInstance;
    }
    
    /**
     * Create component fragment
     * @param options Argument bundle with at least 'component' argument set to describe which
     *                component to build. The options bundle will be passed to the new fragment.
     * @return Built fragment
     */
    public Fragment createFragment(@NonNull Bundle options) {
        Fragment fragment;
        String component;
        
        if(options.getString("component") == null || options.getString("component").isEmpty()) {
            Log.e(TAG, "Component argument not set");
            return null;
        }
        
        component = options.getString("component").toLowerCase(Locale.US);
        Class<? extends Fragment> componentClass = sFragmentTable.get(component);
        if(componentClass != null) {
            Log.v(TAG, "Creating a " + componentClass.getSimpleName());
            try {
                fragment = componentClass.newInstance();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                return null;
            }    
        } else {
            Log.e(TAG, "Component \"" + component + "\" is undefined");
            return null;
        }
        
        fragment.setArguments(options);
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
        if(sMainActivity.get() == null) {
            Log.e(TAG, "switchFragments(): Reference to main activity is null");
            return false;
        }

        String componentTag = args.getString("component");
        boolean isTopLevel = args.getBoolean("topLevel");

        // Create analytics event
        JSONObject extras = new JSONObject();
        try {
            extras.put("handle", componentTag);
            if(args.getString("url") != null) extras.put("url", args.getString("url"));
            if(args.getString("api") != null) extras.put("api", args.getString("api"));
            if(args.getString("title") != null) extras.put("title", args.getString("title"));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Analytics.queueEvent(sMainActivity.get(), Analytics.CHANNEL_OPENED, extras.toString());

        // Attempt to create the fragment
        Fragment fragment = createFragment(args);
        if(fragment == null) return false;

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtils.closeKeyboard(sMainActivity.get());

        FragmentManager fm = sMainActivity.get().getSupportFragmentManager();

        // If this is a top level (nav drawer) press, find the last time this channel was launched
        // and pop backstack to it
        if(isTopLevel && fm.findFragmentByTag(componentTag) != null) {
            fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE); // TODO Not synced with handle stack
        }
                
        // Switch the main content fragment
        fm.beginTransaction()
            .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right)
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
        if(sMainActivity.get() == null) {
            Log.e(TAG, "switchFragments(): Reference to main activity lost");
            return;
        }

        FragmentTransaction ft = sMainActivity.get().getSupportFragmentManager().beginTransaction();
        Fragment prev = sMainActivity.get().getSupportFragmentManager().findFragmentByTag(tag);
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(tag);
        dialogFragment.show(ft, tag);
    }

}
