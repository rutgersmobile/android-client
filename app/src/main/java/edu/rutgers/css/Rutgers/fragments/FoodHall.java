package edu.rutgers.css.Rutgers.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays available meal menus for a dining hall.
 *
 */
public class FoodHall extends Fragment {

	private static final String TAG = "FoodHall";
	private ListView mList;
	private List<String> menus;
	private ArrayAdapter<String> menuAdapter;
	
	public FoodHall() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		
		menus = new ArrayList<String>();
		menuAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.title_row, R.id.title, menus);

		if(args.get("location") == null) {
			Log.e(TAG, "null location");
			return;
		}
		
		Dining.getDiningLocation(args.getString("location")).done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject hall) {
				try {
					JSONArray meals = hall.getJSONArray("meals");
						
					for(int j = 0; j < meals.length(); j++) {
						JSONObject curMeal = meals.getJSONObject(j);
						if(curMeal.getBoolean("meal_avail"))
							menuAdapter.add(curMeal.getString("meal_name"));
					}		
				}
				catch(JSONException e) {
					
					Log.e(TAG, e.getMessage());
				
				}
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_foodhall, parent, false);
		mList = (ListView) v.findViewById(R.id.dining_menu_list);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("location"));
		setupList(args.getString("location"));
		
		return v;
	}
	
	private void setupList(final String location) {
		mList.setAdapter(menuAdapter);
		
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle args = new Bundle();
				args.putString("component", "foodmeal");
				args.putString("location", location);
				args.putString("meal", (String) parent.getAdapter().getItem(position));
				
				ComponentFactory.getInstance().switchFragments(args);
			}
			
		});
	}
}
