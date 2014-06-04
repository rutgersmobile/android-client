package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.jdeferred.DoneCallback;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.Prediction;
import edu.rutgers.css.Rutgers.auxiliary.PredictionAdapter;
import edu.rutgers.css.Rutgers2.R;

public class BusDisplay extends Fragment implements DoneCallback<ArrayList<Prediction>> {

	private static final String TAG = "BusDisplay";
	private enum Mode {ROUTE, STOP};
	
	private ArrayList<Prediction> mData;
	private PredictionAdapter mAdapter;
	private ListView mList;
	private Mode mMode;
	private String mTag;
	private Handler mUpdateHandler;
	private Runnable mUpdateRunnable;
	private Timer mUpdateTimer;
	private String mAgency;
	
	public BusDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<Prediction>();
		mAdapter = new PredictionAdapter(getActivity(), R.layout.bus_predict_row, mData);
		
		Bundle args = getArguments();
		
		// Get title
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		else {
			Log.e(TAG, "title not set");
			getActivity().setTitle(getResources().getString(R.string.bus_title));
		}
		
		// Get agency
		if(args.getString("agency") == null) {
			Log.e(TAG, "agency was not set");
			return;
		}
		else mAgency = args.getString("agency");
		
		// Get mode (route or stop display)
		if(args.getString("mode") == null) {
			Log.e(TAG, "mode was not set");
			return;
		}
		else if(args.getString("mode").equalsIgnoreCase("route")) {
			mMode = Mode.ROUTE;
			mTag = args.getString("tag");
			if(mTag == null) {
				Log.e(TAG, "tag not set for route");
				return;
			}
		}
		else if(args.getString("mode").equalsIgnoreCase("stop")) {
			mMode = Mode.STOP;
			mTag = args.getString("title");
			if(mTag == null) {
				Log.e(TAG, "title tag not set for stop");
				return;
			}
		}
		
		// Setup the timer stuff for updating the bus predictions
		mUpdateTimer = new Timer();
		mUpdateHandler = new Handler();
		mUpdateRunnable = new Runnable() {
			@Override
			public void run() {
				loadPredictions();
			}
		};
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Stop the update thread from running when screen isn't active
		if(mUpdateTimer == null) return;
		
		mUpdateTimer.cancel();
		mUpdateTimer = null;
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
				mUpdateHandler.post(mUpdateRunnable);
			}
		}, 0, 1000 * 60);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busdisplay, parent, false);
		mList = (ListView) v.findViewById(R.id.busDisplayList);
		mList.setAdapter(mAdapter);

		return v;
	}
	
	/**
	 * Load prediction data
	 */
	private void loadPredictions() {
		if(mAgency == null || mTag == null) return;
		
		mAdapter.clear();
		
		if(mMode == Mode.ROUTE) {
			Nextbus.routePredict(mAgency, mTag).then(this);
		}
		else if(mMode == Mode.STOP) {
			Nextbus.stopPredict(mAgency, mTag).then(this);
		}
	}
	
	/**
	 * Callback function for when nextbus data is loaded
	 */
	@Override
	public void onDone(ArrayList<Prediction> predictionArray) {
		for(Prediction p: predictionArray) {
			mAdapter.add(p);
		}
	}
	
}