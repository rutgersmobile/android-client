package edu.rutgers.css.Rutgers.fragments;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.androidquery.AQuery;

import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.JSONArrayAdapter;
import edu.rutgers.css.Rutgers2.R;

public class BusRoutes extends Fragment {
	private AQuery aq;
	private ListView mList;
	private static final String TAG = "BusRoutes";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		aq = new AQuery(this.getActivity());
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busstops, parent, false);
		
		mList = (ListView) v.findViewById(R.id.list);
		Nextbus.getRoutes("nb").then(new DoneCallback<JSONArray>() {
			
			@Override
			public void onDone(JSONArray data) {
				Log.d(TAG, "got data: " + mList);
				JSONArrayAdapter adapter = new JSONArrayAdapter(getActivity(), data, R.layout.title_row);
				mList.setAdapter(adapter);
			}
		});
		
		return v;
	}
	
}
