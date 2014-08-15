package edu.rutgers.css.Rutgers.fragments.Food;

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
import android.widget.Toast;

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
 * Displays available meal mData for a dining hall.
 *
 */
public class FoodHall extends Fragment {

	private static final String TAG = "FoodHall";
    public static final String HANDLE = "foodhall";

	private List<String> mData;
	private ArrayAdapter<String> mAdapter;
	
	public FoodHall() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		
		mData = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.row_title, R.id.title, mData);

		if(args.getString("location") == null) {
			Log.e(TAG, "Location not set");
			return;
		}
		
		Dining.getDiningLocation(args.getString("location")).done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject hall) {
				try {
					JSONArray meals = hall.getJSONArray("meals");
						
					for(int j = 0; j < meals.length(); j++) {
						JSONObject curMeal = meals.getJSONObject(j);
						if(curMeal.getBoolean("meal_avail")) {
                            mAdapter.add(curMeal.getString("meal_name"));
                        }
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
		View v = inflater.inflate(R.layout.fragment_food_hall, parent, false);
		final Bundle args = getArguments();
		if(args.getString("location") != null) {
            getActivity().setTitle(args.getString("location"));
        }
        else {
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location not set");
            return v;
        }

        ListView listView = (ListView) v.findViewById(R.id.dining_menu_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle newArgs = new Bundle();
                newArgs.putString("component", FoodMeal.HANDLE);
                newArgs.putString("location", args.getString("location"));
                newArgs.putString("meal", (String) parent.getAdapter().getItem(position));

                ComponentFactory.getInstance().switchFragments(newArgs);
            }

        });

		return v;
	}

}
