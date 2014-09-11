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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 9/10/14.
 */
public class RecreationHoursDisplay extends Fragment {

    private static final String TAG = "RecreationHoursDisplay";
    private static final String HANDLE = "rechoursdisplay";

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private JSONArray mLocationHours;

    public RecreationHoursDisplay() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_recreation_hours_display, parent, false);

        // Set up pager for hours displays
        mPager = (ViewPager) v.findViewById(R.id.hoursViewPager);
        mPagerAdapter = new HoursSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        // Generate hours array if it wasn't restored
        if(mLocationHours != null) {
            // Get hours data for sub-locations & create fragments
            mPagerAdapter.notifyDataSetChanged();

            // Set swipe page to today's date
            int pos = getCurrentPos(mLocationHours);
            mPager.setCurrentItem(pos, false);

            // Hide hours pager if there's nothing to display
            if(mLocationHours.length() == 0) mPager.setVisibility(View.GONE);
        }

        return v;
    }

    /**
     * A pager adapter which creates fragments to display facility hours.
     */
    private class HoursSlidePagerAdapter extends FragmentStatePagerAdapter {

        public HoursSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                JSONObject data = mLocationHours.getJSONObject(position);
                String date = data.getString("date");
                JSONArray locations = data.getJSONArray("locations");
                return HourSwiperFragment.newInstance(date, locations);
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

    /**
     * Pick default page based on today's date
     * @param locationHours Locations/hours array
     * @return Index of the page for today's date, or 0 if it's not found.
     */
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
