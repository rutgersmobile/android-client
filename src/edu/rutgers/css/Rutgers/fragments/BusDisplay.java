package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.jdeferred.DoneCallback;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
	
	public BusDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<Prediction>();
		mAdapter = new PredictionAdapter(getActivity(), R.layout.bus_predict_row, mData);
		
		Bundle args = getArguments();
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		else {
			getActivity().setTitle("Bus Display");
		}
		
		if(args.getString("mode").equalsIgnoreCase("route")) {
			mMode = Mode.ROUTE;
			mTag = args.getString("tag");
		}
		else if(args.getString("mode").equalsIgnoreCase("stop")) {
			mMode = Mode.STOP;
			mTag = args.getString("title");
		}
		
		mUpdateHandler = new Handler();
		mUpdateRunnable = new Runnable() {
			@Override
			public void run() {
				loadPredictions();
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
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
		mAdapter.clear();
		
		if(mMode == Mode.ROUTE) {
			Nextbus.routePredict("nb", mTag).then(this);
		}
		else if(mMode == Mode.STOP) {
			Nextbus.stopPredict("nb", mTag).then(this);
		}
	}
	
	@Override
	public void onDone(ArrayList<Prediction> predictionArray) {
		for(Prediction p: predictionArray) {
			mAdapter.add(p);
		}
	}
	
}
