package edu.rutgers.css.Rutgers.channels.food.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a menu from the Dining API. Use GSON to construct.
 * @author James Chambers
 */
public class DiningMenu implements Serializable{

    @SerializedName("location_name") private String locationName;
    private long date;
    private List<Meal> meals;

    public DiningMenu(@NonNull String locationName, long date, List<Meal> meals) {
        this.locationName = locationName;
        this.date = date;
        this.meals = meals;
    }

    /** A single meal on the dining menu (breakfast, lunch, etc.). */
    public static class Meal implements Serializable {
        @SerializedName("meal_name") private String mealName;
        @SerializedName("meal_avail") private boolean mealAvailable;
        private List<Genre> genres;

        public Meal(String mealName, boolean mealAvailable, List<Genre> genres) {
            this.mealName = mealName;
            this.mealAvailable = mealAvailable;
            this.genres = genres;
        }

        public String getMealName() {
            return mealName;
        }

        public boolean isMealAvailable() {
            return mealAvailable;
        }

        public List<Genre> getGenres() {
            return genres;
        }

        @Override
        public String toString() {
            return getMealName();
        }
    }

    /** List of foods of a certain category on the dining menu. */
    public static class Genre implements Serializable {
        @SerializedName("genre_name") private String genreName;
        private List<String> items;

        public String getGenreName() {
            return genreName;
        }

        public List<String> getItems() {
            return items;
        }

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

    public String getLocationName() {
        return locationName;
    }

    public Calendar getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    @Override
    public String toString() {
        return getLocationName();
    }

}
