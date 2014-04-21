package edu.rutgers.css.Rutgers.api;

import edu.rutgers.css.Rutgers.SingleFragmentActivity;
import edu.rutgers.css.Rutgers.fragments.BusMain;
import edu.rutgers.css.Rutgers.fragments.DTable;
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
		Log.d("ComponentFactory", "Attempting to create fragment");
		Fragment fragment = new Fragment();
		
		if (options.getString("component").equals("dtable")) {
			fragment = new DTable();
			Log.d("ComponentFactory", "Creating a dtable");
		}
		
		else if (options.getString("component").equals("bus")) {
			fragment = new BusMain();
			Log.d(TAG, "creating a busmain");
		}
		
		else if (options.getString("component").equals("rssreader")) {
			fragment = new RSSReader();
			Log.d(TAG, "creating an rssreader");
		}
		
		else if (options.getString("component").equals("food")) {
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
