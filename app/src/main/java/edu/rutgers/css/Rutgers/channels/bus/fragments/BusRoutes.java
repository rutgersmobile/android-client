package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.jdeferred.multiple.OneResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.Nextbus;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers.utils.RutgersUtil;
import edu.rutgers.css.Rutgers2.R;

public class BusRoutes extends Fragment implements FilterFocusBroadcaster {
    
    private static final String TAG = "BusRoutes";
    public static final String HANDLE = "busroutes";

    private RMenuAdapter mAdapter;
    private ArrayList<RMenuRow> mData;
    private FilterFocusListener mFilterFocusListener;
    private AndroidDeferredManager mDM;
    
    public BusRoutes() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDM = new AndroidDeferredManager();

        mData = new ArrayList<>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bus_routes, parent, false);

        // Get the filter field and add a listener to it
        EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        filterEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(mFilterFocusListener != null) mFilterFocusListener.focusEvent();
            }
        });
        
        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clickedItem = (RMenuItemRow) parent.getAdapter().getItem(position);
                Bundle clickedArgs = clickedItem.getArgs();

                Bundle args = new Bundle(clickedArgs);
                args.putString("component", BusDisplay.HANDLE);
                args.putString("mode", "route");

                ComponentFactory.getInstance().switchFragments(args);
            }

        });

        // Set main bus fragment as focus listener, for switching to All tab
        FilterFocusListener mainFragment = (BusMain) getParentFragment();
        setFocusListener(mainFragment);
                
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear out everything
        mAdapter.clear();

        // Get home campus for result ordering
        String userHome = RutgersUtil.getHomeCampus(getActivity());
        final boolean nbHome = userHome.equals(getString(R.string.campus_nb_full));

        // Get promises for active routes
        final Promise nbActiveRoutes = Nextbus.getActiveRoutes("nb");
        final Promise nwkActiveRoutes = Nextbus.getActiveRoutes("nwk");

        final String nbString = getString(R.string.bus_nb_active_routes_header);
        final String nwkString =  getString(R.string.bus_nwk_active_routes_header);

        // Synchronized load of active routes
        mDM.when(nbActiveRoutes, nwkActiveRoutes).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                // Don't do anything if not attached to activity anymore
                if (!isAdded()) return;
                JSONArray nbResult = null, nwkResult = null;

                for (OneResult result : results) {
                    if (result.getPromise() == nbActiveRoutes) {
                        nbResult = (JSONArray) result.getResult();
                    } else if (result.getPromise() == nwkActiveRoutes) {
                        nwkResult = (JSONArray) result.getResult();
                    }
                }

                if (nbHome) {
                    loadAgency("nb", nbString, nbResult);
                    loadAgency("nwk", nwkString, nwkResult);
                } else {
                    loadAgency("nwk", nwkString, nwkResult);
                    loadAgency("nb", nbString, nbResult);
                }
            }

        }).fail(new FailCallback<OneReject>() {

            @Override
            public void onFail(OneReject result) {
                AppUtil.showFailedLoadToast(getActivity());
                Exception e = (Exception) result.getReject();
                Log.w(TAG, e.getMessage());
            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setFocusListener(null);
    }

    /**
     * Populate list with bus routes for agency, with a section header for that agency
     * @param agencyTag Agency tag for API request
     * @param agencyTitle Header title that goes above these routes
     */
    private void loadAgency(final String agencyTag, final String agencyTitle, final JSONArray data) {
        if(!isAdded() || getResources() == null) return;
        if(data == null) return;

        mAdapter.add(new RMenuHeaderRow(agencyTitle));

        if (data.length() == 0) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_active_routes)));
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject jsonObj = data.getJSONObject(i);
                Bundle menuBundle = new Bundle();
                menuBundle.putString("title", jsonObj.getString("title"));
                menuBundle.putString("tag", jsonObj.getString("tag"));
                menuBundle.putString("agency", agencyTag);
                RMenuItemRow newMenuItem = new RMenuItemRow(menuBundle);
                mAdapter.add(newMenuItem);
            } catch (JSONException e) {
                Log.w(TAG, "loadAgency(): " + e.getMessage());
            }
        }

    }

    @Override
    public void setFocusListener(FilterFocusListener listener) {
        mFilterFocusListener = listener;
    }

}
