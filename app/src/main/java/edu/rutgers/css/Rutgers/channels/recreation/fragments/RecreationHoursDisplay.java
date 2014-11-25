package edu.rutgers.css.Rutgers.channels.recreation.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.recreation.model.FacilityDaySchedule;
import edu.rutgers.css.Rutgers.channels.recreation.model.GymsAPI;

/**
 * Displays facility calendar of hours.
 */
public class  RecreationHoursDisplay extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "RecreationHoursDisplay";
    public static final String HANDLE = "rechoursdisplay";

    /* Argument bundle tags */
    public static final String ARG_TITLE_TAG        = ComponentFactory.ARG_TITLE_TAG;
    public static final String ARG_DATA_TAG         = "rechours.data";

    /* Member data */
    private PagerAdapter mPagerAdapter;
    private List<FacilityDaySchedule> mSchedules;

    public RecreationHoursDisplay() {
        // Required empty public constructor
    }

    /**
     * Create argument bundle for recreation hours display.
     * @param title Title to display (e.g. "$FacilityName - Hours")
     * @param schedules Daily schedules for facility
     */
    public static Bundle createArgs(@NonNull String title, @NonNull List<FacilityDaySchedule> schedules) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RecreationHoursDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);

        // Convert schedules to ArrayList if necessary to ensure that it is serializable
        if(schedules instanceof Serializable) {
            bundle.putSerializable(ARG_DATA_TAG, (Serializable) schedules);
        } else {
            bundle.putSerializable(ARG_DATA_TAG, new ArrayList<>(schedules));
        }

        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();

        // Load location hours
        mSchedules = (List<FacilityDaySchedule>) args.getSerializable(ARG_DATA_TAG);
        if(mSchedules == null) {
            Log.e(TAG, "Hours data not set");
            mSchedules = new ArrayList<>();
        }

        mPagerAdapter = new HoursSlidePagerAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recreation_hours_display, parent, false);

        // Set up pager for hours displays
        ViewPager pager = (ViewPager) v.findViewById(R.id.hoursViewPager);
        pager.setAdapter(mPagerAdapter);

        if(mSchedules != null) {
            mPagerAdapter.notifyDataSetChanged();
            pager.setCurrentItem(getCurrentPos(), false);
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
            FacilityDaySchedule schedule = mSchedules.get(position);
            return HourSwiperFragment.newInstance(schedule.getDate(), schedule.getAreaHours());
        }

        @Override
        public int getCount() {
            return mSchedules.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSchedules.get(position).getDate();
        }

    }

    /**
     * Pick default page based on today's date
     * @return Index of the page for today's date, or 0 if it's not found.
     */
    private int getCurrentPos() {
        String todayString = GymsAPI.GYM_DATE_FORMAT.format(new Date());

        int i = 0;
        for(FacilityDaySchedule schedule: mSchedules) {
            if(todayString.equals(schedule.getDate())) {
                return i;
            } else {
                i++;
            }
        }

        // Date not found, set to last page
        return i;
    }

}
