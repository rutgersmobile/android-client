package edu.rutgers.css.Rutgers.fragments;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationDisplay extends Fragment {

	private static final String TAG = "RecreationDisplay";

	public RecreationDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		final View v = inflater.inflate(R.layout.fragment_recreation_display, parent, false);

		Bundle args = getArguments();
		if(args.getString("campus") == null || args.getString("location") == null) {
			Log.w(TAG, "Missing campus/location arg");
			// TODO Display data fail
			return v;
		}
		
		final String location = args.getString("location");
		final String campus = args.getString("campus");
		
		final TextView addressTextView = (TextView) v.findViewById(R.id.addressTextView);
		final TextView infoDeskNumberTextView = (TextView) v.findViewById(R.id.infoDeskNumberTextView);
		final TextView businessOfficeNumberTextView = (TextView) v.findViewById(R.id.businessOfficeNumberTextView);
		final TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);
		
		Gyms.getGyms().done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject gymsJson) {
				try {
					JSONObject locationJson = gymsJson.getJSONObject(campus).getJSONObject(location);
					addressTextView.setText(locationJson.optString("FacilityAddress"));
					infoDeskNumberTextView.setText(locationJson.optString("FacilityInformation"));
					businessOfficeNumberTextView.setText(locationJson.optString("FacilityBusiness"));
					descriptionTextView.setText(StringEscapeUtils.unescapeHtml4(locationJson.optString("FacilityBody")));
				} catch (JSONException e) {
					Log.w(TAG, "onCreate(): " + e.getMessage());
				}
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				Log.w(TAG, status.getMessage());
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
		
		return v;
	}
	
}
