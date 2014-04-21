package edu.rutgers.css.Rutgers;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class FoodMeal extends Fragment {

	private static final String TAG = "FoodMeal";
	private ListView mList;
	private JSONArray mData;
	private List<String> foodItems;
	private ArrayAdapter<String> foodItemAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		foodItems = new ArrayList<String>();
		foodItemAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, foodItems);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_food_meal, container, false);
		Bundle args = getArguments();
		mList = (ListView) v.findViewById(R.id.food_meal_list);
		
		getActivity().setTitle(args.getString("location"));
		
		Log.d(TAG, "Location: " + args.getString("location") + "; Meal: " + args.getString("meal"));
		setupList(args.getString("location"), args.getString("meal"));
		
		return v;
	}
	
	private void setupList(final String location, final String meal) {
		
		mList.setAdapter(foodItemAdapter);
		
		if(location == null || meal == null) {
			Log.e(TAG, "null location/meal args to setupList");
			return;
		}
		
		// Get JSON array from dining API
		Request.jsonArray("https://rumobile.rutgers.edu/1/rutgers-dining.txt").done(new DoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray data) {
				Log.d(TAG, "Loaded dining data");
				mData = data;
				
				// Successfully grabbed dining data, now get specific location & meal
				JSONObject dinLoc = getDiningLocation(mData, location);
				JSONArray mealGenres = null;
				if(dinLoc != null) mealGenres = getMealGenres(dinLoc, meal);			
				
				// Populate list
				if(mealGenres != null) {
					for(int i = 0; i < mealGenres.length(); i++) {
						try {
							JSONArray mealItems = ((JSONObject) mealGenres.get(i)).getJSONArray("items");
							for(int j = 0; j < mealItems.length(); j++) {
								foodItemAdapter.add(mealItems.getString(j));
							}
						} catch (JSONException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
			
			}
			
		}).fail(new FailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, e.getError() + "; " + e.getMessage() + "; Response code: " + e.getCode());
				mData = null;
			}
			
		});	
	
	}
	
	private JSONObject getDiningLocation(JSONArray diningData, String location) {
		if(diningData == null) {
				Log.e(TAG, "null dining data jsonarray");
				return null;
		}
		
		// Find dining location in dining data
		for(int i = 0; i < diningData.length(); i++) {
			JSONObject curLoc;
			try {
				curLoc = (JSONObject) diningData.get(i);
				if(curLoc.getString("location_name").equalsIgnoreCase(location)) {
					return curLoc;
				}
			} catch (JSONException e) {
				Log.e(TAG, "Could not read location: " + e.getMessage());
				//return null;
			}
		}
		
		Log.e(TAG, "Dining hall location \""+location+"\" not found in Dining API output");
		return null;
	}
	
	private JSONArray getMealGenres(JSONObject diningLocation, String meal) {
		if(diningLocation == null) {
			Log.e(TAG, "null dining location data");
			return null;
		}
		
		try {
			JSONArray meals = diningLocation.getJSONArray("meals");
			
			// Find meal in dining hall data
			for(int i = 0; i < meals.length(); i++) {
				JSONObject curMeal = (JSONObject) meals.get(i);
				
				// Found meal - check if marked available & if so, return genres array
				if(curMeal.getString("meal_name").equalsIgnoreCase(meal)) {
					if(!curMeal.getBoolean("meal_avail")) {
						return null;
					}
					
					return curMeal.getJSONArray("genres");
				}
			}
			
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		
		return null;
	}

}
