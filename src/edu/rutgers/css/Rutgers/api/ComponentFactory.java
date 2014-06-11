package edu.rutgers.css.Rutgers.api;

import java.util.Hashtable;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import edu.rutgers.css.Rutgers.SingleFragmentActivity;
import edu.rutgers.css.Rutgers.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.fragments.BusMain;
import edu.rutgers.css.Rutgers.fragments.DTable;
import edu.rutgers.css.Rutgers.fragments.FoodHall;
import edu.rutgers.css.Rutgers.fragments.FoodMain;
import edu.rutgers.css.Rutgers.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.fragments.PlacesMain;
import edu.rutgers.css.Rutgers.fragments.RSSReader;
import edu.rutgers.css.Rutgers.fragments.RecreationMain;

/**
 * Singleton fragment builder
 *
 */
public class ComponentFactory {

	private static ComponentFactory instance = null;
	public Activity mMainActivity;
	private static final String TAG = "ComponentFactory";
	private Hashtable<String, Class> fragmentTable;
	
	protected ComponentFactory() {
		// Set up table of fragments that can be launched
		fragmentTable = new Hashtable<String, Class>();
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
		Log.d(TAG, "Attempting to create fragment");
		Fragment fragment = new Fragment();
		String component;
		
		if(options.get("component") == null) {
			Log.e(TAG, "Component argument not set");
			return null;
		}
		
		component = options.getString("component").toLowerCase(Locale.US);
		Class compClass = fragmentTable.get(component);
		if(compClass != null) {
			try {
				fragment = (Fragment) compClass.newInstance();
				Log.d(TAG, "Creating a " + compClass.getSimpleName());
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException e) {
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
	
}
