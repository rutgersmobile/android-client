package edu.rutgers.css.Rutgers.fragments.Food;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.items.FoodItem;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers2.R;

/* Dining JSON structure
location_name = "Brower Commons"
meals (array) ->
		meal_name = "Breakfast"
		meal_avail - boolean
		genres (array) ->
			genre_name = "Breakfast Entrees"
			items (array) ->
					"Oatmeal"
					"Pancake
 */

/**
 * Meal display fragment
 * Displays all food items available for a specific meal at a specific dining location.
 */
public class FoodMeal extends Fragment {

	private static final String TAG = "FoodMeal";
    public static final String HANDLE = "foodmeal";

	private List<RMenuRow> foodItems;
	private RMenuAdapter foodItemAdapter;

	public FoodMeal() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		
		foodItems = new ArrayList<RMenuRow>();
		foodItemAdapter = new RMenuAdapter(this.getActivity(), R.layout.row_title, R.layout.row_section_header, foodItems);

		if(args.getString("location") == null) {
            Log.e(TAG, "Location not set");
            return;
        }
        else if(args.getString("meal") == null) {
            Log.e(TAG, "Meal not set");
			return;
		}

        AndroidDeferredManager dm = new AndroidDeferredManager();
		dm.when(Dining.getDiningLocation(args.getString("location"))).done(new DoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject dinLoc) {
				JSONArray mealGenres = Dining.getMealGenres(dinLoc, args.getString("meal"));			
				
				// Populate list
				if(mealGenres != null) {
					
					for(int i = 0; i < mealGenres.length(); i++) {
						try {
							JSONObject curGenre = mealGenres.getJSONObject(i);
							JSONArray mealItems = curGenre.getJSONArray("items");
							foodItemAdapter.add(new FoodItem(curGenre.getString("genre_name"), true)); // Add category header
							
							for(int j = 0; j < mealItems.length(); j++) {
								foodItemAdapter.add(new FoodItem(mealItems.getString(j)));
							}
						} catch (JSONException e) {
							Log.w(TAG, "getDiningLocation(): " + e.getMessage());
						}
					}
					
				}				
			}

		}).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, e.getMessage());
            }

        });
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_food_meal, container, false);

        ListView listView = (ListView) v.findViewById(R.id.food_meal_list);
		listView.setAdapter(foodItemAdapter);
        listView.setOnItemClickListener(null);

		return v;
	}
	
}
