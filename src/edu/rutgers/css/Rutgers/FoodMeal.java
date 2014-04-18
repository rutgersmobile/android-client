package edu.rutgers.css.Rutgers;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
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
	
	private void setupList(String location, String meal) {
		mList.setAdapter(foodItemAdapter);
		foodItemAdapter.add("Super Pancakes");
		
		Request.json("https://rumobile.rutgers.edu/1/rutgers-dining.txt").done(new DoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject data) {
				Log.d(TAG, data.toString());
			}
			
		});	
		
	}

}
