package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Asynchronous loader for meal genres
 */
public class MealGenreLoader extends SimpleAsyncLoader<List<DiningMenu.Genre>> {

    public static final String TAG = "MealGenreLoader";

    private String mealStr;
    private String locationStr;

    public MealGenreLoader(Context context, Bundle args) {
        super(context);
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
        }

        return genres;
    }
}
