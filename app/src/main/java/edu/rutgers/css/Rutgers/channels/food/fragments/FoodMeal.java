package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenuAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.RutgersAPI;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Displays all food items available for a specific meal at a specific dining location.
 * @author James Chambers
 */
public class FoodMeal extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodMeal";
    public static final String HANDLE               = "foodmeal";

    /* Argument bundle tags */
    public static final String ARG_LOCATION_TAG    = "location";
    public static final String ARG_MEAL_TAG        = "meal";

    private static final String ARG_SAVED_DATA_TAG  = "diningGenreData";

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

    /** Create a new instance of the meal display fragment. */
    public static FoodMeal newInstance(@NonNull String location, @NonNull DiningMenu.Meal meal) {
        final FoodMeal foodMeal = new FoodMeal();
        final Bundle args = createArgs(location, meal);
        args.remove(ComponentFactory.ARG_COMPONENT_TAG);
        foodMeal.setArguments(args);
        return foodMeal;
    }

    /** Create argument bundle for a dining hall meal display. */
    public static Bundle createArgs(@NonNull String location, @NonNull DiningMenu.Meal meal) {
        final Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodMeal.HANDLE);
        bundle.putString(ARG_LOCATION_TAG, location);
        bundle.putSerializable(ARG_SAVED_DATA_TAG, meal);
        return bundle;
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
            DiningMenu.Meal savedMeal = (DiningMenu.Meal) args.getSerializable(ARG_SAVED_DATA_TAG);
            if (savedMeal != null) {
                mAdapter.addAll(savedMeal.getGenres());
                return;
            }
            LOGE(TAG, "Meal not set");
            return;
        }

        final String meal = args.getString(FoodMeal.ARG_MEAL_TAG);
        final String location = args.getString(FoodMeal.ARG_LOCATION_TAG);

        if (meal == null || location == null) {
            LOGE(TAG, "Bad arguments");
            return;
        }

        // Start loading meal genres
        RutgersAPI.dining.getDiningHalls()
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            // Map dining halls into stream
            .flatMap(Observable::from)
            // Find correct location and make sure one exists
            .filter(diningMenu -> diningMenu.getLocationName().equals(location)).first()
            // Make sure the meal is not null
            .map(diningMenu -> diningMenu.getMeal(meal))
            .flatMap(foundMeal -> foundMeal == null
                ? Observable.error(new IllegalArgumentException("Bad meal name"))
                : Observable.just(foundMeal.getGenres())
            )
            .subscribe(foundMeal -> {
                reset();
                mAdapter.addAll(foundMeal);
            }, error -> {
                reset();
                LOGE(TAG, error.getMessage());
                AppUtils.showFailedLoadToast(getContext());
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stickylist_progress_simple, parent, false);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);

        return v;
    }

    private void reset() {
        mAdapter.clear();
    }

    @Override
    public Link getLink() {
        return null;
    }
}
