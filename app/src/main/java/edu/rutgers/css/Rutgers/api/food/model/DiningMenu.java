package edu.rutgers.css.Rutgers.api.food.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import lombok.Data;

/**
 * Represents a menu from the Dining API. Use GSON to construct.
 * @author James Chambers
 */
@Data
public class DiningMenu implements Serializable{

    @SerializedName("location_name") private final String locationName;
    private final long date;
    private final List<Meal> meals;

    /** A single meal on the dining menu (breakfast, lunch, etc.). */
    @Data
    public static class Meal implements Serializable {
        @SerializedName("meal_name") private final String mealName;
        @SerializedName("meal_avail") private final boolean mealAvailable;
        private final List<Genre> genres;

        @Override
        public String toString() {
            return getMealName();
        }
    }

    /** List of foods of a certain category on the dining menu. */
    @Data
    public static class Genre implements Serializable {
        @SerializedName("genre_name") private final String genreName;
        private final List<String> items;

        @Override
        public String toString() {
            return getGenreName();
        }
    }

    /**
     * Get a specific meal from the menu.
     * @param mealName Name of the meal to get.
     * @return Meal from dining menu
     */
    public Meal getMeal(@NonNull String mealName) {
        if (meals == null) return null;

        for (Meal meal: meals) {
            if (meal.getMealName().equalsIgnoreCase(mealName)) return meal;
        }

        return null;
    }

    /**
     * Check whether this dining hall has any available meals.
     * @return True if there are available meals, false if not.
     */
    public boolean hasActiveMeals() {
        if (meals == null) return false;

        for (Meal meal: meals) {
            if (meal.isMealAvailable()) return true;
        }

        return false;
    }

    public Calendar getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }

    @Override
    public String toString() {
        return getLocationName();
    }

}
