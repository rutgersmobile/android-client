package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.auxiliary.FoodAdapter;
import edu.rutgers.css.Rutgers.auxiliary.FoodItem;
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
	private ListView mList;
	private List<FoodItem> foodItems;
	private FoodAdapter foodItemAdapter;

	public FoodMeal() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		
		foodItems = new ArrayList<FoodItem>();
		foodItemAdapter = new FoodAdapter(this.getActivity(), R.layout.title_row, R.layout.food_cat_row, foodItems);
		
		if(args.get("location") == null || args.get("meal") == null) {
			Log.e(TAG, "null location/meal");
			return;
		}
		
		Dining.getDiningLocation(args.getString("location")).done(new DoneCallback<JSONObject>() {

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
								JSONObject item = mealItems.getJSONObject(j);
								//some items don't have nutritional info, only name
								int calories;
								String serving;
								String[] ingredients;
								
								if(item.has("calories")){
									calories = item.getInt("calories");
								}else{
									calories = 0;
								}
								
								if(item.has("serving")){
									serving = item.getString("serving");
								}else{
									serving = "";
								}
								
								if(item.has("ingredients")){
									JSONArray jsonIngredients = item.getJSONArray("ingredients");
									ingredients = new String[jsonIngredients.length()];
									for(int k = 0; k < jsonIngredients.length(); k++){
										ingredients[k] = jsonIngredients.getString(k);
									}
								}else{
									ingredients = new String[0];
								}
								
								foodItemAdapter.add(new FoodItem(
										item.getString("name"), calories, serving, ingredients));
							}
							
						} catch (JSONException e) {
						
							Log.e(TAG, e.getMessage());
						
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
		Bundle args = getArguments();
		mList = (ListView) v.findViewById(R.id.food_meal_list);
		mList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				FoodItem item = (FoodItem)(mList.getItemAtPosition(arg2));
				if(item.isCategory){//don't give a popup for categories
					return;
				}
				FoodNutrition newFragment = FoodNutrition.newInstance(item.title, item.calories, item.serving, item.ingredients);
				newFragment.show(getFragmentManager(), "nutrition dialog");
			}
		});
		
		// Set title
		if(args.get("location") != null && args.get("meal") != null)
			getActivity().setTitle(args.getString("location") + " - " + args.getString("meal"));

		mList.setAdapter(foodItemAdapter);

		return v;
	}
	
}
