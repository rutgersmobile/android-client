package edu.rutgers.css.Rutgers.fragments;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.JSONArrayAdapter;
import edu.rutgers.css.Rutgers2.R;

public class BusStops extends Fragment {

	private static final String TAG = "BusStops";
	private ListView mList;
	private JSONArrayAdapter mAdapter;
	private JSONArray mData;
	
	public BusStops() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new JSONArray();
		mAdapter = new JSONArrayAdapter(getActivity(), mData, R.layout.title_row);
		
		Nextbus.getStops("nb").then(new DoneCallback<JSONArray>() {
			
			@Override
			public void onDone(JSONArray data) {
				for(int i = 0; i < data.length(); i++) {
					try {
						mData.put(data.get(i));
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
			
		});
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busstops, parent, false);
		
		mList = (ListView) v.findViewById(R.id.list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				JSONObject clicked = (JSONObject) parent.getAdapter().getItem(position);
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				
				try {
					Bundle args = new Bundle();
					args.putString("component", "busdisplay");
					args.putString("mode", "stop");
					args.putString("title", clicked.getString("title"));
					//args.putString("tag", clicked.getString("tag"));
					
					Fragment fragment = ComponentFactory.getInstance().createFragment(args);
					
					if(fragment != null) {
						fm.beginTransaction()
							.replace(R.id.main_content_frame, fragment)
							.addToBackStack(null)
							.commit();
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
				
			}
			
		});
		
		return v;
	}
	
}
