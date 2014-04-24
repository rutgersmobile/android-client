package edu.rutgers.css.Rutgers.api;

import edu.rutgers.css.Rutgers.SingleFragmentActivity;
import edu.rutgers.css.Rutgers.fragments.BusMain;
import edu.rutgers.css.Rutgers.fragments.DTable;
import edu.rutgers.css.Rutgers.fragments.FoodHall;
import edu.rutgers.css.Rutgers.fragments.FoodMain;
import edu.rutgers.css.Rutgers.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.fragments.RSSReader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class ComponentFactory {

	private static ComponentFactory instance = null;
	public Activity mMainActivity;
	private static final String TAG = "ComponentFactory";

	protected ComponentFactory() {
		
	}
	
	public static ComponentFactory getInstance () {
		if (instance == null) instance = new ComponentFactory();
		return instance;
	}
	
	public Fragment createFragment (Bundle options) {
		Log.d(TAG, "Attempting to create fragment");
		Fragment fragment = new Fragment();
		
		if(options.get("component") == null) {
			Log.e(TAG, "Component argument not set");
			return null;
		}
		
		if (options.getString("component").equals("dtable")) {
			fragment = new DTable();
			Log.d(TAG, "Creating a dtable");
		}
		
		else if (options.getString("component").equals("bus")) {
			fragment = new BusMain();
			Log.d(TAG, "creating a busmain");
		}
		
		else if (options.getString("component").equalsIgnoreCase("reader")) {
			fragment = new RSSReader();
			Log.d(TAG, "creating an rssreader");
		}
		
		else if (options.getString("component").equals("food")) {
			fragment = new FoodMain();
			Log.d(TAG, "creating a foodmeal");
		}
		else if (options.getString("component").equals("foodhall")) {
			fragment = new FoodHall();
			Log.d(TAG, "creating a foodhall");
		}
		else if (options.getString("component").equals("foodmeal")) {
			fragment = new FoodMeal();
			Log.d(TAG, "creating a foodmeal");
		}
		
		fragment.setArguments(options);
		
		Log.d("ComponentFactory", "created fragment");
		
		return fragment;
	}
	
	public void launch (Context c, Bundle options) {
		Intent i = new Intent(c, SingleFragmentActivity.class);
		i.putExtras(options);
		c.startActivity(i);
	}
	
}
