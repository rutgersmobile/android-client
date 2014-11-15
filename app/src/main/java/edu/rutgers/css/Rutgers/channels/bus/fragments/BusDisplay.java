package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.os.Handler;
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

import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.Prediction;
import edu.rutgers.css.Rutgers.channels.bus.model.PredictionAdapter;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers2.R;

public class BusDisplay extends Fragment implements DoneCallback<List<Prediction>>,
        FailCallback<Exception>, AlwaysCallback<List<Prediction>, Exception> {

    private static final String TAG = "BusDisplay";
    public static final String HANDLE = "busdisplay";

    private enum Mode {ROUTE, STOP}
    
    private static final int REFRESH_INTERVAL = 30; // refresh interval in seconds
    
    private ArrayList<Prediction> mData;
    private PredictionAdapter mAdapter;
    private Mode mMode;
    private String mTag;
    private Handler mUpdateHandler;
    private Timer mUpdateTimer;
    private String mAgency;
    private AndroidDeferredManager mDM;
    private ProgressBar mProgressCircle;
    
    public BusDisplay() {
        // Required empty public constructor
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
            mAgency = savedInstanceState.getString("mAgency");
            mTag = savedInstanceState.getString("mTag");
            mMode = (Mode) savedInstanceState.getSerializable("mMode");
            mAdapter.addAll((ArrayList<Prediction>) savedInstanceState.getSerializable("mData"));
            return;
        }

        // Load arguments anew
        final Bundle args = getArguments();

        boolean missingArg = false;
        String requiredArgs[] = {"agency", "mode", "tag"};
        for(String argTag: requiredArgs) {
            if(args.getString(argTag) == null) {
                Log.e(TAG, "Argument \""+argTag+"\" not set");
                missingArg = true;
            }
        }
        if(missingArg) return;

        // Set route or stop display mode
        String mode = args.getString("mode");
        if("route".equalsIgnoreCase(mode)) {
            mMode = Mode.ROUTE;
        } else if("stop".equalsIgnoreCase(mode)) {
            mMode = Mode.STOP;
        } else {
            Log.e(TAG, "Invalid mode \""+args.getString("mode")+"\"");
            // End here and make sure mAgency and mTag are null to prevent update attempts
            return;
        }

        // Get agency and route/stop tag
        mAgency = args.getString("agency");
        mTag = args.getString("tag");

    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bus_display, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

        final Bundle args = getArguments();
        // Get title
        if(args.getString("title") != null) {
            getActivity().setTitle(args.getString("title"));
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
        outState.putString("mAgency", mAgency);
        outState.putString("mTag", mTag);
        outState.putSerializable("mMode", mMode);
        outState.putSerializable("mData", mData);
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
    public void onDone(List<Prediction> predictionArray) {
        hideProgressCircle();

        // If no routes are running through this stop right now, show message
        if(mMode == Mode.STOP && predictionArray.isEmpty()) {
            if(isAdded()) Toast.makeText(getActivity(), R.string.bus_no_active_routes, Toast.LENGTH_SHORT).show();
        }

        /*
         * Add items if the list is being newly populated, or
         * the updated JSON doesn't seem to match and the list should be
         * cleared and repopulated.
         */
        if(mAdapter.getCount() != predictionArray.size()) {
            if(mAdapter.getCount() != 0) {
                Log.w(TAG, "Size of updated list did not match original");
                mAdapter.clear();
            }

            mAdapter.addAll(predictionArray);
        }

        /*
         * Update items individually if the list is already populated
         * and the returned JSON seems valid (matches in size).
         */
        else {
            for(int i = 0; i < mAdapter.getCount(); i++) {    
                Prediction newPrediction = predictionArray.get(i);
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
