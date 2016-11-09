package edu.rutgers.css.Rutgers.api.model.food;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a menu from the Dining API. Use GSON to construct.
 * @author James Chambers
 */
public class DiningMenu implements Serializable {

    @SerializedName("location_name") private final String locationName;
    private final long date;
    private final List<Meal> meals;

    /**
     * Name of dining hall for menu
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Actual menus for each meal
     */
    public List<Meal> getMeals() {
        return meals;
    }

    public DiningMenu(final String locationName, final long date, final List<Meal> meals) {
        this.locationName = locationName;
        this.date = date;
        this.meals = meals;
    }

    /**
     * A single meal on the dining menu (breakfast, lunch, etc.).
     */
    public static class Meal implements Serializable {
        @SerializedName("meal_name") private final String mealName;
        @SerializedName("meal_avail") private final boolean mealAvailable;
        private final List<Genre> genres;

        public Meal(final String mealName, final boolean mealAvailable, final List<Genre> genres) {
            this.mealName = mealName;
            this.mealAvailable = mealAvailable;
            this.genres = genres;
        }

        /**
         * Meal name, ex. "Lunch", "Dinner", etc.
         */
        public String getMealName() {
            return mealName;
        }

        public boolean isMealAvailable() {
            return mealAvailable;
        }

        /**
         * Get categories of meals offered
         */
        public List<Genre> getGenres() {
            return genres;
        }

        @Override
        public String toString() {
            return getMealName();
        }
    }

    /**
     * List of foods of a certain category on the dining menu.
     */
    public static class Genre implements Serializable {
        @SerializedName("genre_name") private final String genreName;
        private final List<String> items;

        public Genre(final String genreName, final List<String> items) {
            this.genreName = genreName;
            this.items = items;
        }

        /**
         * Name of grouping
         */
        public String getGenreName() {
            return genreName;
        }

        /**
         * Name of food offered
         */
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
    public Meal getMeal(String mealName) {
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

    /**
     * When actual meal takes place
     */
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
