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
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.AppUtil;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays dining halls. Selecting a hall goes to meal menu.
 *
 */
public class FoodMain extends Fragment {

	private static final String TAG = "FoodMain";
	private ListView mListView;
	private List<String> mDiningHalls;
	private ArrayAdapter<String> mDiningHallAdapter;

	public FoodMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDiningHalls= new ArrayList<String>();
		mDiningHallAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.title_row, R.id.title, mDiningHalls);
		
		// Get dining hall data and populate the top-level menu with names of the dining halls
		Dining.getDiningHalls().done(new AndroidDoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray halls) {
				try {
					// Only add dining halls which have meals available
					for(int i = 0; i < halls.length(); i++) {
						JSONObject curHall = halls.getJSONObject(i);
						if(Dining.hasActiveMeals(curHall)) {
							mDiningHallAdapter.add(curHall.getString("location_name"));
						}
					}
					
					// Display a message if there no halls were listed
					if(mDiningHallAdapter.getCount() == 0) {
						Toast.makeText(getActivity().getApplicationContext(), R.string.no_halls_available, Toast.LENGTH_SHORT).show();
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
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus e) {
                AppUtil.showFailedLoadToast(getActivity());
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_food_main, parent, false);
		mListView = (ListView) v.findViewById(R.id.dining_locations_list);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("title"));

		mListView.setAdapter(mDiningHallAdapter);		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle args = new Bundle();
				args.putString("component", "foodhall");
				args.putString("location", (String) parent.getAdapter().getItem(position));
				
				ComponentFactory.getInstance().switchFragments(args);
			}
			
		});	
		
		return v;
	}
	
}
