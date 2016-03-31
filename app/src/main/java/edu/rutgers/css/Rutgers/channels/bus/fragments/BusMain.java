package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

public class BusMain extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "BusMain";
    public static final String HANDLE               = "bus";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_START_TAG        = "startTag";

    /* Member data */
    private ViewPager mViewPager;
    private EditText searchBox;
    private TabLayout tabs;
    private boolean searching = false;
    private int position = 0;

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
        if (savedInstanceState != null && searchBox != null) {
            searching = savedInstanceState.getBoolean("searching");
            String search = savedInstanceState.getString("search", "");
            searchBox.setText(search);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchBox != null) {
            outState.putBoolean("searching", true);
            outState.putString("search", searchBox.getText().toString());
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_tabbed_pager, R.id.toolbar_search);
        final Bundle args = getArguments();

        // Set title from JSON
        final String title = args.getString(ARG_TITLE_TAG, getResources().getString(R.string.bus_title));
        getActivity().setTitle(title);

        final BusFragmentPager pagerAdapter = new BusFragmentPager(getChildFragmentManager());

        mViewPager = (ViewPager) v.findViewById(R.id.viewPager);
        mViewPager.setAdapter(pagerAdapter);

        tabs = (TabLayout) v.findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                BusMain.this.position = position;
            }
        });

        int startPage = args.getInt(ARG_START_TAG, -1);
        if (startPage != -1) {
            mViewPager.setCurrentItem(startPage);
        }

        searchBox = (EditText) v.findViewById(R.id.search_box);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_and_share, menu);
        MenuItem searchButton = menu.findItem(R.id.search_button_toolbar);

        if (searching) {
            searchButton.setIcon(R.drawable.ic_clear_black_24dp);
        } else {
            searchButton.setIcon(R.drawable.ic_search_white_24dp);
        }

        int startPage = getArguments().getInt(ARG_START_TAG, -1);
        if (startPage != -1) {
            position = startPage;
        } else {
            position = 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_button_toolbar:
                searching = !searching;
                updateSearchUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        mViewPager = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSearchUI();
    }

    public void updateSearchUI() {
        if (mViewPager != null && searchBox != null) {
            if (searching) {
                searching = true;
                searchBox.setVisibility(View.VISIBLE);
                tabs.setVisibility(View.GONE);
                mViewPager.setCurrentItem(2, true);
                searchBox.requestFocus();
                getToolbar().setBackgroundColor(getResources().getColor(R.color.white));
                AppUtils.openKeyboard(getActivity());
            } else {
                searching = false;
                searchBox.setVisibility(View.GONE);
                tabs.setVisibility(View.VISIBLE);
                searchBox.setText("");
                getToolbar().setBackgroundColor(getResources().getColor(R.color.actbar_new));
                AppUtils.closeKeyboard(getActivity());
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    public void addSearchListener(TextWatcher watcher) {
        searchBox.addTextChangedListener(watcher);
    }

    public Link getLink() {
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

        final List<String> pathParts = new ArrayList<>();
        pathParts.add(tab);
        return new Link("bus", pathParts, getLinkTitle());
    }

    public String getLinkTitle() {
        String posName;
        switch (position) {
            case 0:
                posName = "Routes";
                break;
            case 1:
                posName = "Stops";
                break;
            default:
                posName = "All";
                break;
        }
        return getActivity().getTitle().toString() + " - " + posName;
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
