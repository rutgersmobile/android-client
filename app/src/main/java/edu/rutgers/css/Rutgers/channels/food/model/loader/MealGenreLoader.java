package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.food.DiningAPI;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Asynchronous loader for meal genres
 */
public class MealGenreLoader extends SimpleAsyncLoader<List<DiningMenu.Genre>> {

    public static final String TAG = "MealGenreLoader";

    private String mealStr;
    private String locationStr;

    public MealGenreLoader(Context context, String meal, String location) {
        super(context);
        this.mealStr = meal;
        this.locationStr = location;
    }

    @Override
    public List<DiningMenu.Genre> loadInBackground() {
        final List<DiningMenu.Genre> genres = new ArrayList<>();

        try {
            final DiningMenu diningMenu = DiningAPI.getDiningLocation(locationStr);
            final DiningMenu.Meal meal = diningMenu.getMeal(mealStr);
            if (meal == null) {
                LOGE(TAG, "Meal \"" + mealStr + "\" not found");
            } else {
                // Populate the menu with categories and food items
                final List<DiningMenu.Genre> mealGenres = meal.getGenres();
                genres.addAll(mealGenres);
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return genres;
    }
}
