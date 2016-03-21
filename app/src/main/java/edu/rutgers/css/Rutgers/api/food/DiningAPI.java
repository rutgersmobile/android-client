package edu.rutgers.css.Rutgers.api.food;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Helper for getting data from dining API
 */
public final class DiningAPI {

    private static final String TAG = "DiningAPI";

    private static final String API_PATH = "rutgers-dining.txt";
    private static final int EXPIRE = 1; // Cache dining data for an hour
    private static final TimeUnit EXPIRE_UNIT = TimeUnit.HOURS;

    private static List<DiningMenu> sNBDiningMenus;

    private DiningAPI() {}

    /**
     * Grab the dining API data.
     * <p>(Current API only has New Brunswick data; when multiple confs need to be read set this up like Nextbus.java)</p>
     */
    private static synchronized void setup() throws JsonSyntaxException, IOException {
        Type type = new TypeToken<List<DiningMenu>>(){}.getType();

        try {
            sNBDiningMenus = ApiRequest.api(API_PATH, EXPIRE, EXPIRE_UNIT, type);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, "setup(): " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all dining hall menus.
     * @return List of all dining hall menus
     */
    public static synchronized List<DiningMenu> getDiningHalls() throws JsonSyntaxException, IOException {
        setup();
        return sNBDiningMenus;
    }

    /**
     * Get the menu for a specific dining hall.
     * @param location Dining hall to get menu for
     * @return Promise for the dining hall menu
     */
    public static synchronized DiningMenu getDiningLocation(@NonNull final String location) throws  JsonSyntaxException, IOException {
        setup();

        for (DiningMenu diningMenu : sNBDiningMenus) {
            if (diningMenu.getLocationName().equalsIgnoreCase(location)) {
                return diningMenu;
            }
        }

        // No matching dining hall found
        LOGW(TAG, "Dining hall \"" + location + "\" not found in API.");
        throw new IllegalArgumentException("Dining hall not found");
    }

}
