package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers2.R;
import edu.rutgers.css.Rutgers.api.Dining;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FoodMain extends Fragment {

	private static final String TAG = "FoodMain";
	private ListView mList;
	private List<String> diningHalls;
	private ArrayAdapter<String> diningHallAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		diningHalls= new ArrayList<String>();
		diningHallAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, diningHalls);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_foodmain, parent, false);
		mList = (ListView) v.findViewById(R.id.dining_locations_list);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("title"));
		setupList();
		
		return v;
	}
	
	private void setupList() {
		mList.setAdapter(diningHallAdapter);
		
		Dining.getDiningHalls().done(new DoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray halls) {
				try {
					for(int i = 0; i < halls.length(); i++) {
						JSONObject curHall = halls.getJSONObject(i);
						diningHallAdapter.add(curHall.getString("location_name"));
					}
				}
				catch(JSONException e) {
					Log.e(TAG, e.getMessage());
				}
			}

			
		});
		
	}
}
