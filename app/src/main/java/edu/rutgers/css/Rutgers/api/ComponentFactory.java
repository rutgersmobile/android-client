package edu.rutgers.css.Rutgers.api;

import java.util.Hashtable;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import edu.rutgers.css.Rutgers.SingleFragmentActivity;
import edu.rutgers.css.Rutgers.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.fragments.BusMain;
import edu.rutgers.css.Rutgers.fragments.DTable;
import edu.rutgers.css.Rutgers.fragments.FeedbackMain;
import edu.rutgers.css.Rutgers.fragments.FoodHall;
import edu.rutgers.css.Rutgers.fragments.FoodMain;
import edu.rutgers.css.Rutgers.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.fragments.PlacesMain;
import edu.rutgers.css.Rutgers.fragments.RSSReader;
import edu.rutgers.css.Rutgers.fragments.RUInfoMain;
import edu.rutgers.css.Rutgers.fragments.RecreationDisplay;
import edu.rutgers.css.Rutgers.fragments.RecreationMain;
import edu.rutgers.css.Rutgers.fragments.WebDisplay;
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
		fragmentTable.put("dtable", DTable.class);
		fragmentTable.put("bus", BusMain.class);
		fragmentTable.put("reader", RSSReader.class);
		fragmentTable.put("food", FoodMain.class);
		fragmentTable.put("foodhall", FoodHall.class);
		fragmentTable.put("foodmeal", FoodMeal.class);
		fragmentTable.put("places", PlacesMain.class);
		fragmentTable.put("placesdisplay", PlacesDisplay.class);
		fragmentTable.put("busdisplay", BusDisplay.class);
		fragmentTable.put("recreation", RecreationMain.class);
		fragmentTable.put("recdisplay", RecreationDisplay.class);
		fragmentTable.put("www", WebDisplay.class);
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
	 * @param options Argument bundle with at least 'component' argument set to describe which component to build. All other arguments will be passed to the new fragment.
	 * @return Built fragment
	 */
	public Fragment createFragment (Bundle options) {
		Log.v(TAG, "Attempting to create fragment");
		Fragment fragment = new Fragment();
		String component;
		
		if(options.get("component") == null) {
			Log.e(TAG, "Component argument not set");
			return null;
		}
		
		component = options.getString("component").toLowerCase(Locale.US);
		Class<? extends Fragment> compClass = fragmentTable.get(component);
		if(compClass != null) {
			try {
				fragment = (Fragment) compClass.newInstance();
				Log.v(TAG, "Creating a " + compClass.getSimpleName());
			} catch (Exception e) {
				Log.e(TAG, Log.getStackTraceString(e));
				return null;
			}	
		}
		else {
			Log.e(TAG, "Failed to create component " + component);
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
		
		FragmentManager fm = mMainActivity.getSupportFragmentManager();
		
		// If this is a top level (nav drawer) press, find the last time this channel was launched and pop backstack to it
		if(isTopLevel && fm.findFragmentByTag(componentTag) != null) {
			fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
				
		// Switch the main content fragment
		fm.beginTransaction()
			.replace(R.id.main_content_frame, fragment, componentTag)
			.addToBackStack(componentTag)
			.commit();

		return true;
	}
	
}
