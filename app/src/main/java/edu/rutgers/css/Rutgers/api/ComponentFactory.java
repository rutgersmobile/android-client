package edu.rutgers.css.Rutgers.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

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
	public FragmentActivity mMainActivity;
	private static final String TAG = "ComponentFactory";
	private Hashtable<String, Class<? extends Fragment>> fragmentTable;
	
	protected ComponentFactory() {
		// Set up table of fragments that can be launched
		fragmentTable = new Hashtable<String, Class<? extends Fragment>>();

        // General views
		fragmentTable.put("dtable", DTable.class);
        fragmentTable.put("reader", RSSReader.class);
        fragmentTable.put("text", TextDisplay.class);
        fragmentTable.put("www", WebDisplay.class);

        // Bus views
        fragmentTable.put("bus", BusMain.class);
        fragmentTable.put("busdisplay", BusDisplay.class);

        // Food views
        fragmentTable.put("food", FoodMain.class);
		fragmentTable.put("foodhall", FoodHall.class);
		fragmentTable.put("foodmeal", FoodMeal.class);

        // Places views
		fragmentTable.put("places", PlacesMain.class);
		fragmentTable.put("placesdisplay", PlacesDisplay.class);

        // Recreation views
		fragmentTable.put("recreation", RecreationMain.class);
		fragmentTable.put("recdisplay", RecreationDisplay.class);

        // SOC views
        fragmentTable.put("soc", SOCMain.class);
        fragmentTable.put("soccourses", SOCCourses.class);
        fragmentTable.put("socsections", SOCSections.class);

        // Other views
		fragmentTable.put("ruinfo", RUInfoMain.class);
		fragmentTable.put("feedback", FeedbackMain.class);
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
		if(args == null) {
			Log.e(TAG, "switchFragments(): null args");
			return false;
		}
		
		Fragment fragment = createFragment(args);
		if(fragment == null) return false;
		
		String componentTag = args.getString("component");
		boolean isTopLevel = args.getBoolean("topLevel");

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
	
}
