package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;

import org.jdeferred.DoneCallback;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.Prediction;
import edu.rutgers.css.Rutgers.auxiliary.PredictionAdapter;
import edu.rutgers.css.Rutgers2.R;

public class BusDisplay extends Fragment {

	private static final String TAG = "BusDisplay";
	
	private ArrayList<Prediction> mData;
	private PredictionAdapter mAdapter;
	private ListView mList;
	
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
	
		if(args.getString("mode").equals("route")) {
			Nextbus.routePredict("nb", args.getString("tag")).then(new DoneCallback<ArrayList<Prediction>>() {

				@Override
				public void onDone(ArrayList<Prediction> predictionArray) {
					for(int i = 0; i < predictionArray.size(); i++) {
						mAdapter.add(predictionArray.get(i));
					}
				}
				
			});
		}
		else if(args.getString("mode").equals("stop")) {
			Nextbus.stopPredict("nb", args.getString("title")).then(new DoneCallback<ArrayList<Prediction>>() {

				@Override
				public void onDone(ArrayList<Prediction> predictionArray) {
					for(int i = 0; i < predictionArray.size(); i++) {
						mAdapter.add(predictionArray.get(i));
					}
				}
				
			});
		}
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busdisplay, parent, false);
		mList = (ListView) v.findViewById(R.id.busDisplayList);
		mList.setAdapter(mAdapter);
		
		return v;
	}
	
}
