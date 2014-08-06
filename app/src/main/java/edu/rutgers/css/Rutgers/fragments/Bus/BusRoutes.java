package edu.rutgers.css.Rutgers.fragments.Bus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.jdeferred.multiple.OneResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

public class BusRoutes extends Fragment {
	
	private static final String TAG = "BusRoutes";
	
	private ListView mListView;
	private RMenuAdapter mAdapter;
	private ArrayList<RMenuRow> mData;
	
	public BusRoutes() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mData = new ArrayList<RMenuRow>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bus_routes, parent, false);
		
		mListView = (ListView) v.findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				RMenuItemRow clickedItem = (RMenuItemRow) parent.getAdapter().getItem(position);
				Bundle clickedArgs = clickedItem.getArgs();
				
				try {
					JSONObject clickedJSON = new JSONObject(clickedArgs.getString("json"));

					Bundle args = new Bundle();
					args.putString("component", "busdisplay");
					args.putString("mode", "route");
					args.putString("title", clickedJSON.getString("title"));
					args.putString("tag", clickedJSON.getString("tag"));
					args.putString("agency", clickedArgs.getString("agency"));
					
					ComponentFactory.getInstance().switchFragments(args);
				} catch (JSONException e) {
					Log.e(TAG, "onItemClick(): " + e.getMessage());
				}
				
			}
			
		});
				
		return v;
	}

    @Override
    public void onResume() {
        super.onResume();

        // Update active routes
        mAdapter.clear();

        // Get promises for active routes
        final Promise nbActiveRoutes = Nextbus.getActiveRoutes("nb");
        final Promise nwkActiveRoutes = Nextbus.getActiveRoutes("nwk");

        // Synchronized load of active routes
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(nbActiveRoutes, nwkActiveRoutes).done(new AndroidDoneCallback<MultipleResults>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(MultipleResults results) {
                for (OneResult result : results) {
                    if(result.getPromise() == nbActiveRoutes) loadAgency("nb", getResources().getString(R.string.bus_nb_active_routes_header), (JSONArray) result.getResult());
                    else if(result.getPromise() == nwkActiveRoutes)  loadAgency("nwk", getResources().getString(R.string.bus_nwk_active_routes_header), (JSONArray) result.getResult());
                }
            }

        }).fail(new AndroidFailCallback<OneReject>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onFail(OneReject result) {
                AppUtil.showFailedLoadToast(getActivity());
                Exception e = (Exception) result.getReject();
                Log.w(TAG, e.getMessage());
            }

        });

    }
	
	/**
	 * Populate list with bus routes for agency, with a section header for that agency
	 * @param agencyTag Agency tag for API request
	 * @param agencyTitle Header title that goes above these routes
	 */
	private void loadAgency(final String agencyTag, final String agencyTitle, final JSONArray data) {
        mAdapter.add(new RMenuHeaderRow(agencyTitle));

        if(data == null) {
            mAdapter.add(new RMenuItemRow(getResources().getString(R.string.failed_load_short)));
            return;
        }
        else if (data.length() == 0) {
            mAdapter.add(new RMenuItemRow(getResources().getString(R.string.bus_no_active_routes)));
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject jsonObj = data.getJSONObject(i);
                Bundle menuBundle = new Bundle();
                menuBundle.putString("title", jsonObj.getString("title"));
                menuBundle.putString("json", jsonObj.toString());
                menuBundle.putString("agency", agencyTag);
                RMenuItemRow newMenuItem = new RMenuItemRow(menuBundle);
                mAdapter.add(newMenuItem);
            } catch (JSONException e) {
                Log.w(TAG, "loadAgency(): " + e.getMessage());
            }
        }

    }
	
}
