package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import edu.rutgers.css.Rutgers.MyApplication;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays dining halls. Selecting a hall goes to meal menu.
 *
 */
public class FoodMain extends Fragment {

	private static final String TAG = "FoodMain";
	private ListView mList;
	private List<String> diningHalls;
	private ArrayAdapter<String> diningHallAdapter;

	public FoodMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		diningHalls= new ArrayList<String>();
		diningHallAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.title_row, R.id.title, diningHalls);
		
		// Get dining hall data and populate the top-level menu with names of the dining halls
		Dining.getDiningHalls().done(new DoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray halls) {
				try {
					// Only add dining halls which have meals available
					for(int i = 0; i < halls.length(); i++) {
						JSONObject curHall = halls.getJSONObject(i);
						if(Dining.hasActiveMeals(curHall)) {
							diningHallAdapter.add(curHall.getString("location_name"));
						}
					}
					
					// Display a message if there no halls were listed
					if(diningHallAdapter.getCount() == 0) {
						Toast.makeText(MyApplication.getAppContext(), R.string.no_halls_available, Toast.LENGTH_SHORT).show();
					}
				}
				catch(JSONException e) {
					Log.e(TAG, e.getMessage());
				}
			}

			
		}).fail(new FailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus e) {
				Toast.makeText(MyApplication.getAppContext(), R.string.failed_load, Toast.LENGTH_LONG).show();
			}
			
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_foodmain, parent, false);
		mList = (ListView) v.findViewById(R.id.dining_locations_list);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("title"));

		mList.setAdapter(diningHallAdapter);		
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle args = new Bundle();
				args.putString("component", "foodhall");
				args.putString("location", (String) parent.getAdapter().getItem(position));
				Fragment fragment = ComponentFactory.getInstance().createFragment(args);
				
				if(fragment != null) {
					FragmentManager fm = getActivity().getSupportFragmentManager();
					fm.beginTransaction()
						.replace(R.id.main_content_frame, fragment)
						.addToBackStack(null)
						.commit();
				}
				else {
					Log.e(TAG, "Couldn't launch submenu");
				}
			}
			
		});	
		
		return v;
	}
	
}
