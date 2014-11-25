package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.Prediction;
import edu.rutgers.css.Rutgers.channels.bus.model.PredictionAdapter;
import edu.rutgers.css.Rutgers.utils.AppUtils;

public class BusDisplay extends Fragment implements DoneCallback<List<Prediction>>,
        FailCallback<Exception>, AlwaysCallback<List<Prediction>, Exception> {

    /* Log tag and component handle */
    private static final String TAG = "BusDisplay";
    public static final String HANDLE = "busdisplay";

    /* Constants */
    private enum Mode {ROUTE, STOP}
    private static final int REFRESH_INTERVAL = 30; // refresh interval in seconds

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_AGENCY_TAG      = "agency";
    private static final String ARG_MODE_TAG        = "mode";
    private static final String ARG_TAG_TAG         = "tag";

    /* Argument options */
    public static final String STOP_MODE = "stop";
    public static final String ROUTE_MODE = "route";

    /* Saved instance state tags */
    private static final String SAVED_AGENCY_TAG    = Config.PACKAGE_NAME+"."+HANDLE+".agency";
    private static final String SAVED_TAG_TAG       = Config.PACKAGE_NAME+"."+HANDLE+".tag";
    private static final String SAVED_MODE_TAG      = Config.PACKAGE_NAME+"."+HANDLE+".mode";
    private static final String SAVED_DATA_TAG      = Config.PACKAGE_NAME+"."+HANDLE+".data";

    /* Member data */
    private ArrayList<Prediction> mData;
    private PredictionAdapter mAdapter;
    private Mode mMode;
    private String mTag;
    private Handler mUpdateHandler;
    private Timer mUpdateTimer;
    private String mAgency;
    private AndroidDeferredManager mDM;

    /* View references */
    private ProgressBar mProgressCircle;
    
    public BusDisplay() {
        // Required empty public constructor
    }

    /** Create argument bundle for bus arrival time display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String mode,
                                    @NonNull String agency, @NonNull String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, BusDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_AGENCY_TAG, agency);
        bundle.putString(ARG_MODE_TAG, mode);
        bundle.putString(ARG_TAG_TAG, tag);
        return bundle;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDM = new AndroidDeferredManager();

        mData = new ArrayList<>();
        mAdapter = new PredictionAdapter(getActivity(), mData);

        // Set up handler for bus prediction update timer
        mUpdateHandler = new Handler();

        // Attempt to restore state
        if(savedInstanceState != null) {
            mAgency = savedInstanceState.getString(SAVED_AGENCY_TAG);
            mTag = savedInstanceState.getString(SAVED_TAG_TAG);
            mMode = (Mode) savedInstanceState.getSerializable(SAVED_MODE_TAG);
            mAdapter.addAll((ArrayList<Prediction>) savedInstanceState.getSerializable(SAVED_DATA_TAG));
            return;
        }

        // Load arguments anew
        final Bundle args = getArguments();

        boolean missingArg = false;
        String requiredArgs[] = {ARG_AGENCY_TAG, ARG_MODE_TAG, ARG_TAG_TAG};
        for(String argTag: requiredArgs) {
            if(args.getString(argTag) == null) {
                Log.e(TAG, "Argument \""+argTag+"\" not set");
                missingArg = true;
            }
        }
        if(missingArg) return;

        // Set route or stop display mode
        String mode = args.getString(ARG_MODE_TAG);
        if(BusDisplay.ROUTE_MODE.equalsIgnoreCase(mode)) {
            mMode = Mode.ROUTE;
        } else if(BusDisplay.STOP_MODE.equalsIgnoreCase(mode)) {
            mMode = Mode.STOP;
        } else {
            Log.e(TAG, "Invalid mode \""+args.getString(ARG_MODE_TAG)+"\"");
            // End here and make sure mAgency and mTag are null to prevent update attempts
            return;
        }

        // Get agency and route/stop tag
        mAgency = args.getString(ARG_AGENCY_TAG);
        mTag = args.getString(ARG_TAG_TAG);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bus_display, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

        final Bundle args = getArguments();
        // Get title
        if(args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        } else {
            Log.e(TAG, "Argument \"title\" not set");
            getActivity().setTitle(getString(R.string.bus_title));
        }

        ListView listView = (ListView) v.findViewById(R.id.busDisplayList);

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setAbsListView(listView);
        listView.setAdapter(scaleInAnimationAdapter);

        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Don't run if required args aren't loaded
        if(mAgency == null || mTag == null) return;
        
        // Start the update thread when screen is active
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadPredictions();
                    }
                });
            }
        }, 0, 1000 * REFRESH_INTERVAL);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // Stop the update thread from running when screen isn't active
        if(mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_AGENCY_TAG, mAgency);
        outState.putString(SAVED_TAG_TAG, mTag);
        outState.putSerializable(SAVED_MODE_TAG, mMode);
        outState.putSerializable(SAVED_DATA_TAG, mData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    /**
     * Load prediction data
     */
    private void loadPredictions() {
        // Don't run if required args aren't loaded
        if(mAgency == null || mTag == null) return;

        showProgressCircle();
        if(mMode == Mode.ROUTE) {
            mDM.when(NextbusAPI.routePredict(mAgency, mTag)).then(this).fail(this).always(this);
        } else if(mMode == Mode.STOP) {
            mDM.when(NextbusAPI.stopPredict(mAgency, mTag)).then(this).fail(this).always(this);
        }
    }

    /**
     * Callback function for when nextbus data is loaded
     */
    @Override
    public void onDone(List<Prediction> newPredictions) {
        hideProgressCircle();

        // If no routes are running through this stop right now, show message
        if(mMode == Mode.STOP && newPredictions.isEmpty()) {
            if(isAdded()) Toast.makeText(getActivity(), R.string.bus_no_active_routes, Toast.LENGTH_SHORT).show();
        }

        /*
         * Add items if the list is being newly populated, or
         * the updated JSON doesn't seem to match and the list should be
         * cleared and repopulated.
         */
        if(mAdapter.getCount() != newPredictions.size()) {
            if(mAdapter.getCount() != 0) {
                Log.d(TAG, "Size of updated list did not match original");
                mAdapter.clear();
            }

            mAdapter.addAll(newPredictions);
        }

        /*
         * Update items individually if the list is already populated
         * and the new results correspond to currently displayed stops.
         */
        else {
            for(int i = 0; i < mAdapter.getCount(); i++) {    
                Prediction newPrediction = newPredictions.get(i);
                Prediction oldPrediction = mAdapter.getItem(i);

                if(!newPrediction.equals(oldPrediction)) {
                    Log.d(TAG, "Mismatched prediction: " + oldPrediction.getTitle() + " & " + newPrediction.getTitle());
                    oldPrediction.setTitle(newPrediction.getTitle());
                    oldPrediction.setTag(newPrediction.getTag());
                }
                
                oldPrediction.setMinutes(newPrediction.getMinutes());
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onFail(Exception result) {
        AppUtils.showFailedLoadToast(getActivity());
        mAdapter.clear();
    }

    @Override
    public void onAlways(Promise.State state, List<Prediction> resolved, Exception rejected) {
        hideProgressCircle();
    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
