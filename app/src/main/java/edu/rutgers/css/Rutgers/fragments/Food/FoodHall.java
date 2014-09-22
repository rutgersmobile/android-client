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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays available meal mData for a dining hall.
 *
 */
public class FoodHall extends Fragment {

	private static final String TAG = "FoodHall";
    public static final String HANDLE = "foodhall";

    private String mLocation;
    private MealPagerAdapter mPagerAdapter;
    private JSONObject mData;
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
            String json = savedInstanceState.getString("mData");
            if(json != null) {
                try {
                    mData = new JSONObject(json);
                    loadPages(mData);
                    return;
                } catch (JSONException e) {
                    mData = null;
                }
            }
            mTitle = savedInstanceState.getString("mTitle");
        }

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Dining.getDiningLocation(args.getString("location"))).done(new DoneCallback<JSONObject>() {

            @Override
            public void onDone(JSONObject hall) {
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
        if(mData != null) outState.putString("mData", mData.toString());
        if(mTitle != null) outState.putString("mTitle", mTitle);
    }

    private void loadPages(JSONObject hall) {
        try {
            JSONArray meals = hall.getJSONArray("meals");
            for (int j = 0; j < meals.length(); j++) {
                JSONObject curMeal = meals.getJSONObject(j);
                if (curMeal.getBoolean("meal_avail")) {
                    mPagerAdapter.add(curMeal);
                }
            }

            // Set title to show timestamp for dining data
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(hall.getLong("date"));
            DatePrinter dout = FastDateFormat.getInstance("MMM dd", Locale.US); // Mon, May 26
            mTitle = mLocation + " (" + dout.format(calendar) + ")";
            if(getActivity() != null && AppUtil.isOnTop(FoodHall.HANDLE)) {
                getActivity().setTitle(mTitle);
            }
        } catch (JSONException e) {
            Log.e(TAG, "loadPages(): " + e.getMessage());
        }
    }

    private class MealPagerAdapter extends FragmentStatePagerAdapter {

        private JSONArray mData;

        public MealPagerAdapter(FragmentManager fm) {
            super(fm);
            mData = new JSONArray();
        }

        @Override
        public Fragment getItem(int i) {
            try {
                JSONObject curMeal = mData.getJSONObject(i);
                String mealName = curMeal.getString("meal_name");

                Bundle tabArgs = new Bundle();
                tabArgs.putString("location", mLocation);
                tabArgs.putString("meal", mealName);

                Fragment fragment = new FoodMeal();
                fragment.setArguments(tabArgs);

                return fragment;
            } catch (JSONException e) {
                Log.w(TAG, "getItem(): " + e.getMessage());
                return null;
            }
        }

        @Override
        public int getCount() {
            return mData.length();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                return mData.getJSONObject(position).getString("meal_name");
            } catch (JSONException e) {
                Log.w(TAG, "getPageTitle(): " + e.getMessage());
                return "Meal " + position;
            }
        }

        public void add(JSONObject object) {
            mData.put(object);
            this.notifyDataSetChanged();
        }

    }

}
