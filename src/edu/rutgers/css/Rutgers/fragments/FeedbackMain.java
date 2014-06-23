package edu.rutgers.css.Rutgers.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.res.Resources;
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
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

public class FeedbackMain extends Fragment implements OnItemSelectedListener {

	private static final String TAG = "FeedbackMain";
	private static final String API = "https://rumobile.rutgers.edu/1/feedback.php";
	private static final String OSNAME = "android";
	private static final String BETAMODE = "dev";
	
	private Spinner mSubjectSpinner;
	private EditText mMessageEditText;
	private CheckBox mResponseCheckBox;
	private LinearLayout mSelectChannelLayout;
	
	public FeedbackMain() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_feedback_main, container, false);
		
		getActivity().setTitle(getActivity().getResources().getString(R.string.feedback_title));
		
		mSubjectSpinner = (Spinner) v.findViewById(R.id.subjectSpinner);
		mMessageEditText = (EditText) v.findViewById(R.id.messageEditText);
		mResponseCheckBox = (CheckBox) v.findViewById(R.id.responseCheckBox);
		mSelectChannelLayout = (LinearLayout) v.findViewById(R.id.selectChannelLayout);
		
		mSubjectSpinner.setOnItemSelectedListener(this);
		
		return v;
	}
	
	/**
	 * Submit the feedback
	 */
	private void sendFeedback() {
		// Build POST request
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("subject", mSubjectSpinner.getSelectedItem());
		params.put("email", "user@domain.com");
		params.put("message", mMessageEditText.getText());
		params.put("wants_response", mResponseCheckBox.isChecked());
		params.put("channel", null);
		params.put("debuglog", null);
		params.put("version", null);
		params.put("osname", OSNAME);
		params.put("betamode", BETAMODE);
		
		AQuery aq = new AQuery(getActivity().getApplicationContext());
		aq.ajax(API, params, JSONObject.class, new AjaxCallback<JSONObject>() {
			
			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
				Log.v(TAG, "Response: " + status.getCode() + " / " + json != null ? json.toString() : "null");
			}
			
		});
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Resources res = getActivity().getResources();

		if(parent.getId() == R.id.subjectSpinner) {
			String selection = (String) parent.getItemAtPosition(position);
			
			Log.v(TAG, "Selection " + position + ": " + selection);

			// Channel feedback allows user to select a specific channel
			if(selection.equals(res.getString(R.string.feedback_channel_feedback))) {
				mSelectChannelLayout.setVisibility(View.VISIBLE);
			}
			else {
				mSelectChannelLayout.setVisibility(View.GONE);
			}
			
			// "General questions" boots you to RU-info
			if(selection.equals(getActivity().getResources().getString(R.string.feedback_general))) {
				// Reset selection so that the user can hit back without getting booted right away
				parent.setSelection(0);
				
				// Launch RU-info channel
				Bundle args = new Bundle();
				args.putString("component", "ruinfo");
				args.putString("title", res.getString(R.string.ruinfo_title));
				ComponentFactory.getInstance().switchFragments(args);
			}

		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
}
