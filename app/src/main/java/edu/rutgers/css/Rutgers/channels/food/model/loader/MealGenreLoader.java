package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import lombok.val;

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
        val genres = new ArrayList<DiningMenu.Genre>();

        try {
            val diningMenu = DiningAPI.getDiningLocation(locationStr);
            val meal = diningMenu.getMeal(mealStr);
            if (meal == null) {
                LOGE(TAG, "Meal \"" + mealStr + "\" not found");
            } else {
                // Populate the menu with categories and food items
                val mealGenres = meal.getGenres();
                genres.addAll(mealGenres);
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return genres;
    }
}
