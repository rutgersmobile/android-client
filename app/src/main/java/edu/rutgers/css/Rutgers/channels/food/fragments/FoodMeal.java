package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenuAdapter;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Displays all food items available for a specific meal at a specific dining location.
 * @author James Chambers
 */
public class FoodMeal extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "FoodMeal";
    public static final String HANDLE = "foodmeal";

    /* Argument bundle tags */
    private static final String ARG_LOCATION_TAG    = "location";
    private static final String ARG_MEAL_TAG        = "meal";

    /* Member data */
    private DiningMenuAdapter mAdapter;

    public FoodMeal() {
        // Required empty public constructor
    }

    /** Create a new instance of the meal display fragment. */
    public static FoodMeal newInstance(@NonNull String location, @NonNull String meal) {
        FoodMeal foodMeal = new FoodMeal();
        Bundle args = createArgs(location, meal);
        args.remove(ComponentFactory.ARG_COMPONENT_TAG);
        foodMeal.setArguments(args);
        return foodMeal;
    }

    /** Create argument bundle for a dining hall meal display. */
    public static Bundle createArgs(@NonNull String location, @NonNull String meal) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodMeal.HANDLE);
        bundle.putString(ARG_LOCATION_TAG, location);
        bundle.putString(ARG_MEAL_TAG, meal);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        
        mAdapter = new DiningMenuAdapter(getActivity(),
                R.layout.row_title, R.layout.row_section_header, R.id.title);

        if(args.getString(ARG_LOCATION_TAG) == null) {
            Log.e(TAG, "Location not set");
            return;
        } else if(args.getString(ARG_MEAL_TAG) == null) {
            Log.e(TAG, "Meal not set");
            return;
        }

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(DiningAPI.getDiningLocation(args.getString(ARG_LOCATION_TAG))).done(new DoneCallback<DiningMenu>() {

            @Override
            public void onDone(DiningMenu diningMenu) {
                DiningMenu.Meal meal = diningMenu.getMeal(args.getString(ARG_MEAL_TAG));
                if (meal == null) {
                    Log.e(TAG, "Meal \"" + args.getString(ARG_MEAL_TAG) + "\" not found");
                    return;
                }

                // Populate the menu with categories and food items
                List<DiningMenu.Genre> mealGenres = meal.getGenres();
                mAdapter.addAll(mealGenres);
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, e.getMessage());
                AppUtils.showFailedLoadToast(getActivity());
            }

        });
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_food_meal, parent, false);

        StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);

        return v;
    }
}
