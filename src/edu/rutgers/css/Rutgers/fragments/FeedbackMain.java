package edu.rutgers.css.Rutgers.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

public class FeedbackMain extends Fragment implements OnItemSelectedListener {

	private static final String TAG = "FeedbackMain";
	
	public FeedbackMain() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_feedback_main, container, false);
		
		getActivity().setTitle(getActivity().getResources().getString(R.string.feedback_title));
		
		final Spinner subjectSpinner = (Spinner) v.findViewById(R.id.subjectSpinner);
		final EditText messageEditText = (EditText) v.findViewById(R.id.messageEditText);
		final CheckBox responseCheckBox = (CheckBox) v.findViewById(R.id.responseCheckBox);
		
		subjectSpinner.setOnItemSelectedListener(this);
		
		return v;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		if(parent.getId() == R.id.subjectSpinner) {
			String selection = (String) parent.getItemAtPosition(position);
			
			Log.v(TAG, "Selection " + position + ": " + selection);
			
			// "General questions" boots you to RU-info
			if(selection.equals(getActivity().getResources().getString(R.string.feedback_general))) {
				// Reset selection so that the user can hit back without getting booted right away
				parent.setSelection(0);
				
				// Launch RU-info channel
				Bundle args = new Bundle();
				args.putString("component", "ruinfo");
				args.putString("title", getActivity().getResources().getString(R.string.ruinfo_title));
				ComponentFactory.getInstance().switchFragments(args);
			}
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
}
