package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationMain extends Fragment {

	private static final String TAG = "RecreationMain";
	private static final String API_URL = "https://rumobile.rutgers.edu/1/gyms.txt";
	
	private ArrayList<RMenuPart> mData;
	private ListView mListView;
	private RMenuAdapter mAdapter;
	
	public RecreationMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<RMenuPart>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.basic_item, R.layout.basic_section_header, mData);
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		final View v = inflater.inflate(R.layout.fragment_recreation_main, parent, false);
		
		mListView = (ListView) v.findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		
		return v;
	}
	
}
