package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenuAdapter;
import edu.rutgers.css.Rutgers.channels.food.model.loader.MealGenreLoader;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Displays all food items available for a specific meal at a specific dining location.
 * @author James Chambers
 */
public class FoodMeal extends Fragment
    implements LoaderManager.LoaderCallbacks<List<DiningMenu.Genre>> {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodMeal";
    public static final String HANDLE               = "foodmeal";

    /* Argument bundle tags */
    public static final String ARG_LOCATION_TAG    = "location";
    public static final String ARG_MEAL_TAG        = "meal";

    private static final int LOADER_ID              = TAG.hashCode();

    /* Member data */
    private DiningMenuAdapter mAdapter;

    public FoodMeal() {
        // Required empty public constructor
    }

    /** Create a new instance of the meal display fragment. */
    public static FoodMeal newInstance(@NonNull String location, @NonNull String meal) {
        final FoodMeal foodMeal = new FoodMeal();
        final Bundle args = createArgs(location, meal);
        args.remove(ComponentFactory.ARG_COMPONENT_TAG);
        foodMeal.setArguments(args);
        return foodMeal;
    }

    /** Create argument bundle for a dining hall meal display. */
    public static Bundle createArgs(@NonNull String location, @NonNull String meal) {
        final Bundle bundle = new Bundle();
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

        if (args.getString(ARG_LOCATION_TAG) == null) {
            LOGE(TAG, "Location not set");
            return;
        } else if (args.getString(ARG_MEAL_TAG) == null) {
            LOGE(TAG, "Meal not set");
            return;
        }

        // Start loading meal genres
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stickylist_progress, parent, false);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public Loader<List<DiningMenu.Genre>> onCreateLoader(int id, Bundle args) {
        final String meal = args.getString(FoodMeal.ARG_MEAL_TAG);
        final String location = args.getString(FoodMeal.ARG_LOCATION_TAG);
        return new MealGenreLoader(getContext(), meal, location);
    }

    @Override
    public void onLoadFinished(Loader<List<DiningMenu.Genre>> loader, List<DiningMenu.Genre> data) {
        // Assume and empty response is an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
        mAdapter.clear();
        mAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<DiningMenu.Genre>> loader) {
        mAdapter.clear();
    }
}
