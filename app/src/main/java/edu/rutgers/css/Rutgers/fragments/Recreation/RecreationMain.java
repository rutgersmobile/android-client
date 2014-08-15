package edu.rutgers.css.Rutgers.fragments.Recreation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationMain extends Fragment {

	private static final String TAG = "RecreationMain";
    public static final String HANDLE = "recreation";
	
	private ArrayList<RMenuRow> mData;
	private RMenuAdapter mAdapter;
	
	public RecreationMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<RMenuRow>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);
		
		Gyms.getGyms().done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject gymsJson) {
				try {
					// Loop through campuses
					Iterator<String> campusIter = gymsJson.keys();
					while(campusIter.hasNext()) {
						String curCampusKey = campusIter.next();
						mAdapter.add(new RMenuHeaderRow(curCampusKey));
						
						// Loop through locations on this campus
						JSONObject curCampus = gymsJson.getJSONObject(curCampusKey);
						Iterator<String> locationIter = curCampus.keys();
						while(locationIter.hasNext()) {
							String curLocationKey = locationIter.next();
							JSONObject curLocation = curCampus.getJSONObject(curLocationKey);
							
							Bundle args = new Bundle();
							args.putString("title", curLocationKey);
							args.putString("campus", curCampusKey);
							args.putString("location", curLocationKey);
							
							mAdapter.add(new RMenuItemRow(args));
						}
						
					}
				} catch (JSONException e) {
					Log.w(TAG, "onCreate(): " + e.getMessage());
				}
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				Log.w(TAG, status.getMessage());
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		final View v = inflater.inflate(R.layout.fragment_recreation_main, parent, false);
        Bundle args = getArguments();

        // Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.rec_title);

		ListView listView = (ListView) v.findViewById(R.id.list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getItemAtPosition(position);
                Bundle args = clicked.getArgs();
                args.putString("component", RecreationDisplay.HANDLE);

                ComponentFactory.getInstance().switchFragments(args);
            }

        });
		
		return v;
	}
	
}
