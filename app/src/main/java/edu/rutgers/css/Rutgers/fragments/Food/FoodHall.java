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

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	public FoodHall() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        mLocation = args.getString("location");
        if(mLocation == null) return;

        mPagerAdapter = new MealPagerAdapter(getChildFragmentManager());

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Dining.getDiningLocation(args.getString("location"))).done(new DoneCallback<JSONObject>() {

            @Override
            public void onDone(JSONObject hall) {
                try {
                    JSONArray meals = hall.getJSONArray("meals");
                    for (int j = 0; j < meals.length(); j++) {
                        JSONObject curMeal = meals.getJSONObject(j);
                        if (curMeal.getBoolean("meal_avail")) {
                            mPagerAdapter.add(curMeal);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
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

		if(args.getString("location") != null) {
            getActivity().setTitle(args.getString("location"));
        }
        else {
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location not set");
            return v;
        }

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(mPagerAdapter);

		return v;
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
                Log.w(TAG, e.getMessage());
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
                return "";
            }
        }

        public void add(JSONObject object) {
            mData.put(object);
            this.notifyDataSetChanged();
        }

    }

}
