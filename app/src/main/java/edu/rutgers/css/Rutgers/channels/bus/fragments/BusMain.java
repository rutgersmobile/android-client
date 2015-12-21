package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.lang.ref.WeakReference;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.LinkUtils;

public class BusMain extends Fragment implements FilterFocusListener {

    /* Log tag and component handle */
    private static final String TAG                 = "BusMain";
    public static final String HANDLE               = "bus";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_START_TAG        = "startTag";

    /* Member data */
    private ViewPager mViewPager;
    private WeakReference<BusAll> mAllTab;
    private ShareActionProvider shareActionProvider;

    public BusMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main bus screen. */
    public static Bundle createArgs(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, BusMain.HANDLE);
        if (title != null) bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    public static Bundle createArgs(String title, int startTag) {
        Bundle bundle = createArgs(title);
        bundle.putInt(ARG_START_TAG, startTag);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        mViewPager = (ViewPager) v.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new BusFragmentPager(getChildFragmentManager()));

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (shareActionProvider != null) {
                    BusMain.this.setShareIntent(position);
                }
                if (position == 2) {
                    // When "All" tab is selected, focus the search field and open keyboard
                    if (mAllTab != null && mAllTab.get() != null) {
                        mAllTab.get().focusFilter();
                    }
                } else {
                    // When another tab is scrolled to, close the keyboard
                    AppUtils.closeKeyboard(getActivity());
                }
            }
        });

        int startPage = args.getInt(ARG_START_TAG, -1);
        if (startPage != -1) {
            mViewPager.setCurrentItem(startPage);
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_link, menu);
        MenuItem shareItem = menu.findItem(R.id.deep_link_share);
        if (shareItem != null) {
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            int startPage = getArguments().getInt(ARG_START_TAG, -1);
            if (startPage != -1) {
                setShareIntent(startPage);
            } else {
                setShareIntent(0);
            }
        }
    }

    private void setShareIntent(int position) {
        String tab;
        switch (position) {
            case 0:
                tab = "route";
                break;
            case 1:
                tab = "stop";
                break;
            default:
                tab = "all";
                break;
        }

        Uri uri = LinkUtils.buildUri(Config.SCHEMA, "bus", tab);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        shareActionProvider.setShareIntent(intent);
    }

    @Override
    public void onDestroyView() {
        mViewPager = null;
        mAllTab = null;
        super.onDestroyView();
    }

    @Override
    public void focusEvent() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void registerAllTab(BusAll allTab) {
        mAllTab = new WeakReference<>(allTab);
    }

    private class BusFragmentPager extends FragmentPagerAdapter {

        public BusFragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new BusRoutes();
                case 1:
                    return new BusStops();
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
