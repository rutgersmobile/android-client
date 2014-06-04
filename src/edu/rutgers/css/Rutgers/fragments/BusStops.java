package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuHeader;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers2.R;

public class BusStops extends Fragment {

	private static final String TAG = "BusStops";
	private ListView mList;
	private RMenuAdapter mAdapter;
	private ArrayList<RMenuPart> mData;
	
	public BusStops() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<RMenuPart>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.title_row, R.layout.main_drawer_header, mData);
		
		//TODO Nearby stops!
		
		loadAgency("nb", getResources().getString(R.string.bus_nb_active_stops_header));
		loadAgency("nwk", getResources().getString(R.string.bus_nwk_active_stops_header));
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busstops, parent, false);
		
		mList = (ListView) v.findViewById(R.id.list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SlideMenuItem clickedItem = (SlideMenuItem) parent.getAdapter().getItem(position);
				Bundle clickedArgs = clickedItem.getArgs();
				
				try {
					JSONObject clickedJSON = new JSONObject(clickedArgs.getString("json"));
				
					FragmentManager fm = getActivity().getSupportFragmentManager();
				
					Bundle args = new Bundle();
					args.putString("component", "busdisplay");
					args.putString("mode", "stop");
					args.putString("title", clickedJSON.getString("title"));
					args.putString("agency", clickedArgs.getString("agency"));
					
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
	
	/**
	 * Populate list with bus stops for agency, with a section header for that agency
	 * @param agencyTag Agency tag for API request
	 * @param agencyTitle Header title that goes above these stops
	 */
	private void loadAgency(final String agencyTag, final String agencyTitle) {
		Nextbus.getStops(agencyTag).then(new DoneCallback<JSONArray>() {
			
			@Override
			public void onDone(JSONArray data) {
				Log.d(TAG, data.toString());
				
				mAdapter.add(new SlideMenuHeader(agencyTitle));
				for(int i = 0; i < data.length(); i++) {
					try {
						JSONObject jsonObj = data.getJSONObject(i);
						Bundle menuBundle = new Bundle();
						menuBundle.putString("title", jsonObj.getString("title"));
						menuBundle.putString("json", jsonObj.toString());
						menuBundle.putString("agency", agencyTag);
						SlideMenuItem newMenuItem = new SlideMenuItem(menuBundle);
						mAdapter.add(newMenuItem);
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
			
		});
	}
	
}
