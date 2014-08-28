package edu.rutgers.css.Rutgers.api;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Reader;
import java.util.Hashtable;
import java.util.Locale;

import edu.rutgers.css.Rutgers.SingleFragmentActivity;
import edu.rutgers.css.Rutgers.fragments.Bus.BusDisplay;
import edu.rutgers.css.Rutgers.fragments.Bus.BusMain;
import edu.rutgers.css.Rutgers.fragments.DTable;
import edu.rutgers.css.Rutgers.fragments.FeedbackMain;
import edu.rutgers.css.Rutgers.fragments.Food.FoodHall;
import edu.rutgers.css.Rutgers.fragments.Food.FoodMain;
import edu.rutgers.css.Rutgers.fragments.Food.FoodMeal;
import edu.rutgers.css.Rutgers.fragments.Places.PlacesDisplay;
import edu.rutgers.css.Rutgers.fragments.Places.PlacesMain;
import edu.rutgers.css.Rutgers.fragments.RSSReader;
import edu.rutgers.css.Rutgers.fragments.RUInfoMain;
import edu.rutgers.css.Rutgers.fragments.Recreation.RecreationDisplay;
import edu.rutgers.css.Rutgers.fragments.Recreation.RecreationMain;
import edu.rutgers.css.Rutgers.fragments.SOC.SOCCourses;
import edu.rutgers.css.Rutgers.fragments.SOC.SOCMain;
import edu.rutgers.css.Rutgers.fragments.SOC.SOCSections;
import edu.rutgers.css.Rutgers.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Singleton fragment builder
 *
 */
public class ComponentFactory {

	private static ComponentFactory instance = null;
	private FragmentActivity mMainActivity;
	private static final String TAG = "ComponentFactory";
	private Hashtable<String, Class<? extends Fragment>> fragmentTable;
	
	protected ComponentFactory() {
		// Set up table of fragments that can be launched
		fragmentTable = new Hashtable<String, Class<? extends Fragment>>();

        // General views
		fragmentTable.put(DTable.HANDLE, DTable.class);
        fragmentTable.put(RSSReader.HANDLE, RSSReader.class);
        fragmentTable.put(TextDisplay.HANDLE, TextDisplay.class);
        fragmentTable.put(WebDisplay.HANDLE, WebDisplay.class);

        // Bus views
        fragmentTable.put(BusMain.HANDLE, BusMain.class);
        fragmentTable.put(BusDisplay.HANDLE, BusDisplay.class);

        // Food views
        fragmentTable.put(FoodMain.HANDLE, FoodMain.class);
		fragmentTable.put(FoodHall.HANDLE, FoodHall.class);
		fragmentTable.put(FoodMeal.HANDLE, FoodMeal.class);

        // Places views
		fragmentTable.put(PlacesMain.HANDLE, PlacesMain.class);
		fragmentTable.put(PlacesDisplay.HANDLE, PlacesDisplay.class);

        // Recreation views
		fragmentTable.put(RecreationMain.HANDLE, RecreationMain.class);
		fragmentTable.put(RecreationDisplay.HANDLE, RecreationDisplay.class);

        // SOC views
        fragmentTable.put(SOCMain.HANDLE, SOCMain.class);
        fragmentTable.put(SOCCourses.HANDLE, SOCCourses.class);
        fragmentTable.put(SOCSections.HANDLE, SOCSections.class);

        // Other views
		fragmentTable.put(RUInfoMain.HANDLE, RUInfoMain.class);
		fragmentTable.put(FeedbackMain.HANDLE, FeedbackMain.class);
	}

    public void setMainActivity(FragmentActivity fragmentActivity) {
        this.mMainActivity = fragmentActivity;
    }

	/**
	 * Get singleton instance
	 * @return Component factory singleton instance
	 */
	public static ComponentFactory getInstance () {
		if (instance == null) instance = new ComponentFactory();
		return instance;
	}
	
	/**
	 * Create component fragment
	 * @param options Argument bundle with at least 'component' argument set to describe which component to build. The options bundle will be passed to the new fragment.
	 * @return Built fragment
	 */
	public Fragment createFragment (Bundle options) {
		Fragment fragment;
		String component;
		
		if(options.getString("component") == null || options.getString("component").isEmpty()) {
			Log.e(TAG, "Component argument not set");
			return null;
		}
		
		component = options.getString("component").toLowerCase(Locale.US);
		Class<? extends Fragment> compClass = fragmentTable.get(component);
		if(compClass != null) {
            Log.v(TAG, "Creating a " + compClass.getSimpleName());
			try {
				fragment = compClass.newInstance();
			} catch (Exception e) {
				Log.e(TAG, Log.getStackTraceString(e));
				return null;
			}	
		}
		else {
			Log.e(TAG, "Component \"" + component + "\" is undefined");
			return null;
		}
		
		fragment.setArguments(options);
		return fragment;
	}
	
	/**
	 * Launch an activity
	 * @param context
	 * @param options Argument bundle
	 */
	public void launch (Context context, Bundle options) {
		Intent i = new Intent(context, SingleFragmentActivity.class);
		i.putExtras(options);
		context.startActivity(i);
	}
	
	/**
	 * Add current fragment to the backstack and switch to the new one defined by given arguments.
	 * For calls from the nav drawer, this will attempt to pop all backstack history until the last 
	 * time the desired channel was launched.
	 * @param args Argument bundle with at least 'component' argument set to describe which component to build. All other arguments will be passed to the new fragment.
	 * @return True if the new fragment was successfully created, false if not.
	 */
	public boolean switchFragments(Bundle args) {
        if(mMainActivity == null) {
            Log.e(TAG, "switchFragments(): main activity not set");
            return false;
        }
		else if(args == null) {
			Log.e(TAG, "switchFragments(): null args");
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
        Analytics.queueEvent(mMainActivity, Analytics.CHANNEL_OPENED, extras.toString());

        // Attempt to create the fragment
        Fragment fragment = createFragment(args);
		if(fragment == null) return false;

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtil.closeKeyboard(mMainActivity);

        FragmentManager fm = mMainActivity.getSupportFragmentManager();

        // If this is a top level (nav drawer) press, find the last time this channel was launched and pop backstack to it
		if(isTopLevel && fm.findFragmentByTag(componentTag) != null) {
			fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
    public void showDialogFragment(DialogFragment dialogFragment, String tag) {
        FragmentTransaction ft = mMainActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mMainActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, tag);
    }
}
