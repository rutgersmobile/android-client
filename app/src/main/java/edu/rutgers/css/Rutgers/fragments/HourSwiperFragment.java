package edu.rutgers.css.Rutgers.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rutgers.css.Rutgers2.R;

public class HourSwiperFragment extends Fragment {

	private static final String TAG = "HourSwiperFragment";
	
	public HourSwiperFragment() {
		// Required empty public constructor
	}
	
	public static HourSwiperFragment newInstance(String date, JSONObject hours) {
		HourSwiperFragment newFrag =  new HourSwiperFragment();
		Bundle args = new Bundle();
		args.putString("date", date);
		args.putString("hours", hours.toString());
		newFrag.setArguments(args);
		return newFrag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.hour_swiper, container, false);
		Bundle args = getArguments();
		
		LinearLayout hourSwiperTableLayout = (LinearLayout) v.findViewById(R.id.hourSwiperTableLayout);
		
		// Add hours rows here
		try {
			JSONObject hours = new JSONObject(args.getString("hours"));
			Iterator<String> keys = hours.keys();
			while(keys.hasNext()) {
				String curLocationKey = keys.next();
				TableRow newTR = (TableRow) inflater.inflate(R.layout.hour_row, container, false);

                // Set the sub-location title. Wrap at ~18 chars in portrait mode.
                TextView sublocTextView = (TextView) newTR.findViewById(R.id.sublocTextView);
                switch(getResources().getConfiguration().orientation) {
                    case Configuration.ORIENTATION_PORTRAIT:
                        sublocTextView.setText(WordUtils.wrap(curLocationKey,18));
                        break;
                    default:
                        // The word wrap is unnecessary in landscape mode
                        sublocTextView.setText(curLocationKey);
                        break;
                }

                // Set the hours list for sub-location.
                TextView hoursTextView = (TextView) newTR.findViewById(R.id.hoursTextView);

                // Sometimes these are comma separated, sometimes not  ¯\_(ツ)_/¯
                String hoursString = StringUtils.trim(hours.getString(curLocationKey));
                if(StringUtils.startsWithIgnoreCase(hoursString, "closed")) {
                    hoursTextView.setText(hoursString);
                }
                else if(StringUtils.countMatches(hoursString, ",") > 0) {
                    hoursTextView.setText(hoursString.replace(",", "\n"));
                }
                else {
                    StringBuilder builder = new StringBuilder();
                    int matches = 0;

                    // Here we go...
                    Pattern pattern = Pattern.compile("\\d{1,2}(\\:\\d{2})?\\s?((A|P)M)?\\s?-\\s?\\d{1,2}(\\:\\d{2})?\\s?(A|P)M"); // Why? Because "9:30 - 11:30AM/7 - 10PM"
                    Matcher matcher = pattern.matcher(hoursString);
                    while(matcher.find()) {
                        //Log.v(TAG, "Found " + matcher.group() + " at ("+matcher.start()+","+matcher.end()+")");
                        builder.append(StringUtils.trim(matcher.group()) + "\n");
                        matches++;
                    }

                    if(matches > 0) {
                        hoursTextView.setText(StringUtils.chomp(builder.toString()));
                    }
                    else {
                        // ಥ_ಥ
                        hoursTextView.setText(hoursString);
                    }
                }

				hourSwiperTableLayout.addView(newTR);
			}
		} catch(JSONException e) {
			Log.w(TAG, "onCreateView(): " + e.getMessage());
		}
		
		return v;
	}
	
}
