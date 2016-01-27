package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.loader.DiningMenuLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.link.LinkMaps;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Displays available meals for a dining hall.
 * @author James Chambers
 */
public class FoodHall extends BaseChannelFragment
    implements LoaderManager.LoaderCallbacks<DiningMenu> {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodHall";
    public static final String HANDLE               = "foodhall";

    /* Argument bundle tags */
    public static final String ARG_TITLE_TAG        = ComponentFactory.ARG_TITLE_TAG;

    /* Saved instance state tags */
    private static final String ARG_SAVED_DATA_TAG  = "diningMenuData";

    public static final int LOADER_ID               = AppUtils.getUniqueLoaderId();

    /* Member data */
    private String mTitle;
    private String mLocation;
    private MealPagerAdapter mPagerAdapter;
    private DiningMenu mData;
    private ShareActionProvider shareActionProvider;
    private TabLayout tabLayout;

    private final static DatePrinter dout = FastDateFormat.getInstance("MMM dd", Locale.US); // Mon, May 26

    public FoodHall() {
        // Required empty public constructor
    }

    public static Bundle createArgs(@NonNull String location) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodHall.HANDLE);
        bundle.putString(ARG_TITLE_TAG, location);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();

        mLocation = args.getString(ARG_TITLE_TAG);
        if (mLocation == null) {
            LOGE(TAG, "location argument not set");
            return;
        }

        mPagerAdapter = new MealPagerAdapter(getChildFragmentManager());

        if (savedInstanceState != null) {
            mData = (DiningMenu) savedInstanceState.getSerializable(ARG_SAVED_DATA_TAG);
        }

        if (mData == null) {
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, savedInstanceState, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_tabbed_pager);
        final Bundle args = getArguments();

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        if (mTitle != null) {
            getActivity().setTitle(mTitle);
        } else if (args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        } else {
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            LOGE(TAG, "Location not set");
            return v;
        }

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(mPagerAdapter);

        tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        tabLayout.setTabsFromPagerAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        if (mData != null) {
            loadPages(mData);
        }

        return v;
    }

    @Override
    public ShareActionProvider getShareActionProvider() {
        return shareActionProvider;
    }

    @Override
    public Link getLink() {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add(LinkMaps.diningHallsInv.get(mLocation));
        return new Link("food", pathParts, getLinkTitle());
    }

    @Override
    public String getLinkTitle() {
        return mLocation;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mData != null) outState.putSerializable(ARG_SAVED_DATA_TAG, mData);
    }

    private void loadPages(DiningMenu diningMenu) {
        mPagerAdapter.clear();
        mPagerAdapter.addAllAvailable(diningMenu.getMeals());

        if (tabLayout != null) {
            tabLayout.setTabsFromPagerAdapter(mPagerAdapter);
        }

        // Set title to show timestamp for dining data
        mTitle = mLocation + " (" + dout.format(diningMenu.getDate()) + ")";
        if (getActivity() != null && AppUtils.isOnTop(getActivity(), FoodHall.HANDLE)) {
            getActivity().setTitle(mTitle);
        }
    }

    @Override
    public Loader<DiningMenu> onCreateLoader(int id, Bundle args) {
        return new DiningMenuLoader(getActivity(), mLocation);
    }

    @Override
    public void onLoadFinished(Loader<DiningMenu> loader, DiningMenu data) {
        mPagerAdapter.clear();
        // Data will only be null if there is an error
        if (data == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }
        mData = data;
        loadPages(mData);
    }

    @Override
    public void onLoaderReset(Loader<DiningMenu> loader) {
        mPagerAdapter.clear();
    }

    private class MealPagerAdapter extends FragmentPagerAdapter {

        private List<DiningMenu.Meal> mData;

        public MealPagerAdapter(FragmentManager fm) {
            super(fm);
            mData = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int i) {
            return FoodMeal.newInstance(mLocation, mData.get(i));
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).getMealName();
        }

        public void add(DiningMenu.Meal meal) {
            mData.add(meal);
            this.notifyDataSetChanged();
        }

        public void addAllAvailable(Collection<DiningMenu.Meal> meals) {
            List<DiningMenu.Meal> availMeals = new ArrayList<>();
            for (final DiningMenu.Meal meal : meals) {
                if (meal.isMealAvailable()) {
                    availMeals.add(meal);
                }
            }

            mData.addAll(availMeals);
            notifyDataSetChanged();
        }

        public void clear() {
            mData.clear();
            this.notifyDataSetChanged();
        }
    }
}
