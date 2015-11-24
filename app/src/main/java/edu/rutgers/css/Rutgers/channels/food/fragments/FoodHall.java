package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.loader.DiningMenuLoader;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Displays available meals for a dining hall.
 * @author James Chambers
 */
public class FoodHall extends Fragment
    implements LoaderManager.LoaderCallbacks<DiningMenu> {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodHall";
    public static final String HANDLE               = "foodhall";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_LOCATION_TAG    = "location";

    /* Saved instance state tags */
    private static final String SAVED_DATA_TAG    = Config.PACKAGE_NAME + ".dtable.saved.data";

    public static final int LOADER_ID               = TAG.hashCode();

    /* Member data */
    private String mTitle;
    private String mLocation;
    private MealPagerAdapter mPagerAdapter;
    private DiningMenu mData;

    private final static DatePrinter dout = FastDateFormat.getInstance("MMM dd", Locale.US); // Mon, May 26

    public FoodHall() {
        // Required empty public constructor
    }

    public static Bundle createArgs(@NonNull String location) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodHall.HANDLE);
        bundle.putString(ARG_TITLE_TAG, location);
        bundle.putString(ARG_LOCATION_TAG, location);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        mLocation = args.getString(ARG_LOCATION_TAG);
        if (mLocation == null) {
            LOGE(TAG, "location argument not set");
            return;
        }

        mPagerAdapter = new MealPagerAdapter(getChildFragmentManager());

        mData = (DiningMenu) args.getSerializable(SAVED_DATA_TAG);
        if (mData == null) {
            getLoaderManager().initLoader(LOADER_ID, savedInstanceState, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tabbed_pager, parent, false);
        final Bundle args = getArguments();

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

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mData != null) outState.putSerializable(SAVED_DATA_TAG, mData);
    }

    private void loadPages(DiningMenu diningMenu) {
        List<DiningMenu.Meal> meals = diningMenu.getMeals();
        for (DiningMenu.Meal meal: meals) {
            if (meal.isMealAvailable()) mPagerAdapter.add(meal);
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
            DiningMenu.Meal curMeal = mData.get(i);
            String mealName = curMeal.getMealName();
            return FoodMeal.newInstance(mLocation, mealName);
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

        public void clear() {
            mData.clear();
            this.notifyDataSetChanged();
        }

    }

}
