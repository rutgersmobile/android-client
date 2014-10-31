package edu.rutgers.css.Rutgers.fragments.Recreation;

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

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rutgers.css.Rutgers.items.Recreation.MeetingAreaHours;
import edu.rutgers.css.Rutgers2.R;

/**
 * Fragment for an individual page of facility hours.
 */
public class HourSwiperFragment extends Fragment {

    private static final String TAG = "HourSwiperFragment";

    public static final String DATE_TAG = "date";
    public static final String LOCATIONS_TAG = "locations";
    
    public HourSwiperFragment() {
        // Required empty public constructor
    }
    
    public static HourSwiperFragment newInstance(String date, List<MeetingAreaHours> areaHours) {
        HourSwiperFragment newFrag =  new HourSwiperFragment();
        Bundle args = new Bundle();
        args.putString(DATE_TAG, date);
        args.putSerializable(LOCATIONS_TAG, (Serializable) areaHours);
        newFrag.setArguments(args);
        return newFrag;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hour_swiper, container, false);
        Bundle args = getArguments();
        
        LinearLayout hourSwiperTableLayout = (LinearLayout) v.findViewById(R.id.hourSwiperTableLayout);

        List<MeetingAreaHours> areaHours = (List<MeetingAreaHours>) args.getSerializable(LOCATIONS_TAG);
        if(areaHours == null) {
            Log.e(TAG, "Area hours not set");
            return v;
        }

        // Create new table row for each area listing
        for(MeetingAreaHours area: areaHours) {
            TableRow newTR = (TableRow) inflater.inflate(R.layout.hour_row, container, false);

            // Set the sub-location title. Wrap at ~18 chars in portrait mode.
            String locationTitle = area.getLocation();

            TextView sublocTextView = (TextView) newTR.findViewById(R.id.sublocTextView);
            switch(getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    sublocTextView.setText(WordUtils.wrap(locationTitle,18));
                    break;
                default:
                    // The word wrap is unnecessary in landscape mode
                    sublocTextView.setText(locationTitle);
                    break;
            }

            // Set the hours list for sub-location.
            TextView hoursTextView = (TextView) newTR.findViewById(R.id.hoursTextView);

            // Sometimes these are comma separated, sometimes not  ¯\_(ツ)_/¯
            String hoursString = StringUtils.trim(area.getHours());

            if(StringUtils.startsWithIgnoreCase(hoursString, "closed")) {
                setHoursTextView(hoursTextView, hoursString);
            } else if(StringUtils.countMatches(hoursString, ",") > 0) {
                setHoursTextView(hoursTextView, hoursString.replace(",", "\n"));
            } else {
                StringBuilder builder = new StringBuilder();
                int matches = 0;

                // This matches something like "9:00AM - 10:00AM" with or without spaces between
                // any group, with the first AM/PM and minutes optional because sometimes there
                // are entries like "9:30 - 11:30AM/7 - 10PM".
                Pattern pattern = Pattern.compile("\\d{1,2}(\\:\\d{2})?\\s?((A|P)M)?\\s?-\\s?\\d{1,2}(\\:\\d{2})?\\s?(A|P)M");
                Matcher matcher = pattern.matcher(hoursString);
                while(matcher.find()) {
                    //Log.v(TAG, "Found " + matcher.group() + " at ("+matcher.start()+","+matcher.end()+")");
                    builder.append(StringUtils.trim(matcher.group())).append("\n");
                    matches++;
                }

                if(matches > 0) {
                    setHoursTextView(hoursTextView, StringUtils.chomp(builder.toString()));
                } else {
                    // :'( All is lost. A single tear is shed.
                    setHoursTextView(hoursTextView, hoursString);
                }
            }

            hourSwiperTableLayout.addView(newTR);
        }

        return v;
    }

    private void setHoursTextView(TextView hoursTextView, String string) {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hoursTextView.setText(string);
        } else {
            hoursTextView.setText(WordUtils.wrap(string, 18).replaceAll("\\n\\n","\n"));
        }
    }

}
