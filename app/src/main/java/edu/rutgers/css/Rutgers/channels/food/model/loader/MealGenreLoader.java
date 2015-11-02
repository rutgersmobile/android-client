package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Asynchronous loader for meal genres
 */
public class MealGenreLoader extends AsyncTaskLoader<List<DiningMenu.Genre>> {

    public static final String TAG = "MealGenreLoader";

    private Context context;
    private String mealStr;
    private String locationStr;
    private List<DiningMenu.Genre> mData;

    public MealGenreLoader(Context context, Bundle args) {
        super(context);
        this.context = context;
        this.mealStr = args.getString(FoodMeal.ARG_MEAL_TAG);
        this.locationStr = args.getString(FoodMeal.ARG_LOCATION_TAG);
    }

    @Override
    public List<DiningMenu.Genre> loadInBackground() {
        final List<DiningMenu.Genre> genres = new ArrayList<>();

        try {
            DiningMenu diningMenu = DiningAPI.getDiningLocation(locationStr);
            DiningMenu.Meal meal = diningMenu.getMeal(mealStr);
            if (meal == null) {
                LOGE(TAG, "Meal \"" + mealStr + "\" not found");
            } else {
                // Populate the menu with categories and food items
                List<DiningMenu.Genre> mealGenres = meal.getGenres();
                genres.addAll(mealGenres);
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            AppUtils.showFailedLoadToast(context);
        }

        return genres;
    }

    @Override
    public void deliverResult(List<DiningMenu.Genre> genres) {
        if (isReset()) {
            return;
        }

        List<DiningMenu.Genre> oldItems = mData;
        mData = genres;
        if (isStarted()) {
            super.deliverResult(mData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mData = null;
    }
}
