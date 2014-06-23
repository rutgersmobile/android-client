package edu.rutgers.css.Rutgers.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.Spinner;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

public class FeedbackMain extends Fragment implements OnItemSelectedListener {

	private static final String TAG = "FeedbackMain";
	//private static final String API = "https://rumobile.rutgers.edu/1/feedback.php";
	private static final String API = "http://sauron.rutgers.edu/~jamchamb/feedback.php";
	private static final String OSNAME = "android";
	private static final String BETAMODE = "dev";
	
	private Spinner mSubjectSpinner;
	private Spinner mChannelSpinner;
	private EditText mMessageEditText;
	private CheckBox mResponseCheckBox;
	private LinearLayout mSelectChannelLayout;
	private boolean mLockSend;
	
	private AQuery aq;
	
	public FeedbackMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		aq = new AQuery(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_feedback_main, container, false);
		
		getActivity().setTitle(getActivity().getResources().getString(R.string.feedback_title));
		
		mLockSend = false;
		
		mSubjectSpinner = (Spinner) v.findViewById(R.id.subjectSpinner);
		mChannelSpinner = (Spinner) v.findViewById(R.id.channelSpinner);
		mMessageEditText = (EditText) v.findViewById(R.id.messageEditText);
		mResponseCheckBox = (CheckBox) v.findViewById(R.id.responseCheckBox);
		mSelectChannelLayout = (LinearLayout) v.findViewById(R.id.selectChannelLayout);
		
		mSubjectSpinner.setOnItemSelectedListener(this);
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.feedback_menu, menu);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	// Handle send button
    	if(item.getItemId() == R.id.action_send) {
    		if(!mLockSend) sendFeedback();
    		return true;
    	}
    	
    	return false;
    }
    	
	/**
	 * Submit the feedback
	 */
	private void sendFeedback() {
		Resources res = getActivity().getResources();
		
		// Empty message - do nothing
		if(mMessageEditText.getText().equals("")) {
			return;
		}
		
		// Build POST request
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("subject", mSubjectSpinner.getSelectedItem());
		params.put("email", "appuser@nowhere.null");
		params.put("message", mMessageEditText.getText());
		params.put("wants_response", mResponseCheckBox.isChecked());
		if(mSubjectSpinner.getSelectedItem().equals(res.getString(R.string.feedback_channel_feedback))) {
			params.put("channel", mChannelSpinner.getSelectedItem());	
		}
		params.put("debuglog", null);
		params.put("version", "0.0");
		params.put("osname", OSNAME);
		params.put("betamode", BETAMODE);
		
		// Lock send button until POST request goes through
		mLockSend = true;
		
		aq.ajax(API, params, JSONObject.class, new AjaxCallback<JSONObject>() {
			
			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
				// Unlock send button
				mLockSend = false;
				
				if(status != null) Log.v(TAG, "Response: " + status.getCode() + " - " + status.getMessage());
				else Log.e(TAG, "No AJAX status");
				
				// Check the response JSON
				if(json != null) {
					Log.v(TAG, "json: " + json.toString());
					
					// Errors - invalid input
					if(json.optJSONArray("errors") != null) {
						JSONArray response = json.optJSONArray("errors");
						
						Log.v(TAG, "Feedback POST failed:");
						for(int i = 0; i < response.length(); i++) {
							Log.v(TAG, "   "+response.optString(i));
						}
					}
					// Success - input went through
					else if(json.optString("success") != null) {
						String response = json.optString("success");
						
						Log.v(TAG, "Feedback POST success:");
						Log.v(TAG, "   " + response);
						
						// Only reset forms after message has gone through
						resetForm();
					}
					
				}
				
			}
			
		});

	}
	
	private void resetForm() {
		// Reset the form
		mSubjectSpinner.setSelection(0);
		mChannelSpinner.setSelection(0);
		mResponseCheckBox.setChecked(false);
		mMessageEditText.setText("");
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Resources res = getActivity().getResources();

		if(parent.getId() == R.id.subjectSpinner) {
			String selection = (String) parent.getItemAtPosition(position);
			
			// Channel feedback allows user to select a specific channel
			if(selection.equals(res.getString(R.string.feedback_channel_feedback))) {
				mSelectChannelLayout.setVisibility(View.VISIBLE);
			}
			else {
				mSelectChannelLayout.setVisibility(View.GONE);
			}
			
			// "General questions" boots you to RU-info
			if(selection.equals(res.getString(R.string.feedback_general))) {
				// Reset selection so that the user can hit back without getting booted right away
				// (this means general questions can never be the default option!)
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
