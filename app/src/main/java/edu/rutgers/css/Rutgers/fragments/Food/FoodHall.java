package edu.rutgers.css.Rutgers.fragments.Food;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.items.DiningMenu;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays available meals for a dining hall.
 * @author James Chambers
 */
public class FoodHall extends Fragment {

    private static final String TAG = "FoodHall";
    public static final String HANDLE = "foodhall";

    private final static DatePrinter dout = FastDateFormat.getInstance("MMM dd", Locale.US); // Mon, May 26

    private String mLocation;
    private MealPagerAdapter mPagerAdapter;
    private DiningMenu mData;
    private String mTitle;

    public FoodHall() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mLocation = args.getString("location");
        if(mLocation == null) return;

        mPagerAdapter = new MealPagerAdapter(getChildFragmentManager());

        if(savedInstanceState != null) {
            mData = (DiningMenu) savedInstanceState.getSerializable("mData");
            loadPages(mData);
            return;
        }

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Dining.getDiningLocation(args.getString("location"))).done(new DoneCallback<DiningMenu>() {
            @Override
            public void onDone(DiningMenu hall) {
                mData = hall;
                loadPages(mData);
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtil.showFailedLoadToast(getActivity());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_food_hall, parent, false);
        final Bundle args = getArguments();

        if(mTitle != null) {
            getActivity().setTitle(mTitle);
        } else if(args.getString("location") != null) {
            getActivity().setTitle(args.getString("location"));
        } else {
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location not set");
            return v;
        }

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(mPagerAdapter);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mData != null) outState.putSerializable("mData", mData);
    }

    private void loadPages(DiningMenu diningMenu) {
        List<DiningMenu.Meal> meals = diningMenu.getMeals();
        for(DiningMenu.Meal meal: meals) {
            if(meal.isMealAvailable()) mPagerAdapter.add(meal);
        }

        // Set title to show timestamp for dining data
        mTitle = mLocation + " (" + dout.format(diningMenu.getDate()) + ")";
        if(getActivity() != null && AppUtil.isOnTop(FoodHall.HANDLE)) {
            getActivity().setTitle(mTitle);
        }
    }

    private class MealPagerAdapter extends FragmentStatePagerAdapter {

        private List<DiningMenu.Meal> mData;

        public MealPagerAdapter(FragmentManager fm) {
            super(fm);
            mData = new ArrayList<DiningMenu.Meal>();
        }

        @Override
        public Fragment getItem(int i) {
            DiningMenu.Meal curMeal = mData.get(i);
            String mealName = curMeal.getMealName();

            Bundle tabArgs = new Bundle();
            tabArgs.putString("location", mLocation);
            tabArgs.putString("meal", mealName);

            Fragment fragment = new FoodMeal();
            fragment.setArguments(tabArgs);

            return fragment;
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

    }

}
