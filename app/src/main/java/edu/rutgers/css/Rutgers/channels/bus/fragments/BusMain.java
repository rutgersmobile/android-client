package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;

public class BusMain extends Fragment implements FilterFocusListener {

    /* Log tag and component handle */
    private static final String TAG = "BusMain";
    public static final String HANDLE = "bus";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private ViewPager mViewPager;

    public BusMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main bus screen. */
    public static Bundle createArgs(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, BusMain.HANDLE);
        if(title != null) bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tabbed_pager, parent, false);
        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        } else {
            getActivity().setTitle(R.string.bus_title);
        }

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(new BusFragmentPager(getChildFragmentManager(), this));

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);

        return v;
    }

    @Override
    public void onDestroyView() {
        mViewPager = null;
        super.onDestroyView();
    }

    @Override
    public void focusEvent() {
        if(mViewPager != null) {
            mViewPager.setCurrentItem(2, true);
        }
    }

    private class BusFragmentPager extends FragmentPagerAdapter {

        private FilterFocusListener focusListener;

        public BusFragmentPager(FragmentManager fm, FilterFocusListener focusListener) {
            super(fm);
            this.focusListener = focusListener;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    BusRoutes busRoutes = new BusRoutes();
                    busRoutes.setFocusListener(focusListener);
                    return busRoutes;
                case 1:
                    BusStops busStops = new BusStops();
                    busStops.setFocusListener(focusListener);
                    return busStops;
                case 2:
                    return new BusAll();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return getString(R.string.bus_routes_tab);
                case 1:
                    return getString(R.string.bus_stops_tab);
                case 2:
                    return getString(R.string.bus_all_tab);
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

    }

}
