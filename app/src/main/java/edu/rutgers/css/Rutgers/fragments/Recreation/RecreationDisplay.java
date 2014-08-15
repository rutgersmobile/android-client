package edu.rutgers.css.Rutgers.fragments.Recreation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationDisplay extends Fragment {

	private static final String TAG = "RecreationDisplay";
    public static final String HANDLE = "recdisplay";

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private JSONArray mLocationHours;
    private JSONObject mLocationJSON;
	
	public RecreationDisplay() {
		// Required empty public constructor
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            String jsonString, hoursString;
            jsonString = savedInstanceState.getString("mLocationJSON");
            hoursString = savedInstanceState.getString("mLocationHours");

            try {
                if(jsonString != null) mLocationJSON = new JSONObject(jsonString);
                if(hoursString != null) mLocationHours = new JSONArray(hoursString);
            } catch (JSONException e) {
                Log.w(TAG, "onCreate(): " + e.getMessage());
            }
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		final View v = inflater.inflate(R.layout.fragment_recreation_display, parent, false);

		// Make sure necessary arguments were given
		Bundle args = getArguments();
		if(args.getString("campus") == null || args.getString("location") == null) {
			Log.w(TAG, "Missing campus/location arg");
			AppUtil.showFailedLoadToast(getActivity());
			return v;
		}
		
		getActivity().setTitle(args.getString("location"));
		
		// Set up pager for hours displays
		mPager = (ViewPager) v.findViewById(R.id.hoursViewPager);
		mPagerAdapter = new HoursSlidePagerAdapter(getChildFragmentManager());
		mPager.setAdapter(mPagerAdapter);

        // If location JSON was saved in state, display info now.
        if(mLocationJSON != null) {
            displayInfo(v);
            return v;
        }

        // Don't have JSON yet - do request
		final String location = args.getString("location");
		final String campus = args.getString("campus");
		Gyms.getGyms().done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject gymsJson) {
				try {
					// Get location JSON
					mLocationJSON = gymsJson.getJSONObject(campus).getJSONObject(location);
                    displayInfo(v);
				} catch (JSONException e) {
					Log.w(TAG, "onCreateView(): " + e.getMessage());
				}
				
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				AppUtil.showFailedLoadToast(getActivity());
                Log.w(TAG, status.getMessage());
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});

		return v;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLocationJSON != null) outState.putString("mLocationJSON", mLocationJSON.toString());
        if(mLocationHours != null) outState.putString("mLocationHours", mLocationHours.toString());
    }

    private void displayInfo(View v) {

        // Get view IDs
        final TextView addressTextView = (TextView) v.findViewById(R.id.addressTextView);
        final TextView infoDeskNumberTextView = (TextView) v.findViewById(R.id.infoDeskNumberTextView);
        final TextView businessOfficeNumberTextView = (TextView) v.findViewById(R.id.businessOfficeNumberTextView);
        final TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);

        final TableRow infoHeadRow = (TableRow) v.findViewById(R.id.infoRowHead);
        final TableRow infoContentRow = (TableRow) v.findViewById(R.id.infoRowContent);
        final TableRow businessHeadRow = (TableRow) v.findViewById(R.id.businessRowHead);
        final TableRow businessContentRow = (TableRow) v.findViewById(R.id.businessRowContent);


        // Fill in location info
        String infoDesk = mLocationJSON.optString("FacilityInformation");
        String businessOffice = mLocationJSON.optString("FacilityBusiness");

        addressTextView.setText(mLocationJSON.optString("FacilityAddress"));

        if(infoDesk != null && !infoDesk.isEmpty()) {
            infoDeskNumberTextView.setText(infoDesk);
        }
        else {
            infoHeadRow.setVisibility(View.GONE);
            infoContentRow.setVisibility(View.GONE);
        }

        if(businessOffice != null && !businessOffice.isEmpty()) {
            businessOfficeNumberTextView.setText(businessOffice);
        }
        else {
            businessHeadRow.setVisibility(View.GONE);
            businessContentRow.setVisibility(View.GONE);
        }

        descriptionTextView.setText(StringEscapeUtils.unescapeHtml4(mLocationJSON.optString("FacilityBody")));

        try {
            // Generate hours array if it wasn't restored
            if(mLocationHours == null) {
                // Get hours data for sub-locations & create fragments
                mLocationHours = Gyms.getGymHours(mLocationJSON);
                mPagerAdapter.notifyDataSetChanged();

                // Set swipe page to today's date
                int pos = getCurrentPos(mLocationHours);
                mPager.setCurrentItem(pos, true);
            }

            // Hide hours pager if there's nothing to display
            if(mLocationHours.length() == 0) mPager.setVisibility(View.GONE);
        } catch (JSONException e) {
            Log.w(TAG, "displayInfo(): " + e.getMessage());
        }

    }

	private class HoursSlidePagerAdapter extends FragmentStatePagerAdapter {
		
		public HoursSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			JSONObject data;
			try {
				data = mLocationHours.getJSONObject(position);
				String date = data.getString("date");
				JSONObject hours = data.getJSONObject("hours");
				if(hours == null) return null;
				return HourSwiperFragment.newInstance(date, hours);
			} catch (JSONException e) {
				Log.w(TAG, "getItem(): " + e.getMessage());
				return null;
			}
		}

		@Override
		public int getCount() {
			if(mLocationHours == null) return 0;
			else return mLocationHours.length();
		}
		
        @Override
        public CharSequence getPageTitle(int position) {
            try {
				return mLocationHours.getJSONObject(position).getString("date");
			} catch (JSONException e) {
				Log.w(TAG, "getPageTitle(): " + e.getMessage());
				return "";
			}
        }
		
	}
	
	private int getCurrentPos(JSONArray locationHours) {
		String todayString = Gyms.GYM_DATE_FORMAT.format(new Date());
		for(int i = 0; i < locationHours.length(); i++) {
			try {
				if(locationHours.getJSONObject(i).getString("date").equals(todayString)) return i;
			} catch (JSONException e) {
				Log.w(TAG, "getCurrentPos(): " + e.getMessage());
				return 0;
			}
		}
		
		return 0;
	}

}
