package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import lombok.val;

/**
 * Loads dining menus in a new thread
 */
public class DiningMenuSectionLoader extends SimpleAsyncLoader<List<SimpleSection<DiningMenu>>> {

    public static final String TAG = "DiningMenuSectionLoader";

    String nbCampusFullString;
    String nwkCampusFullString;
    String camCampusFullString;

    public DiningMenuSectionLoader(Context context) {
        super(context);
        nbCampusFullString = context.getString(R.string.campus_nb_full);
        nwkCampusFullString = context.getString(R.string.campus_nwk_full);
        camCampusFullString = context.getString(R.string.campus_cam_full);
    }

    @Override
    public List<SimpleSection<DiningMenu>> loadInBackground() {
        val simpleSections = new ArrayList<SimpleSection<DiningMenu>>();
        // Get user's home campus
        val userHome = RutgersUtils.getHomeCampus(getContext());

        // Static dining hall entries
        val dummyMeal = new ArrayList<DiningMenu.Meal>(1);
        dummyMeal.add(new DiningMenu.Meal("fake", true, null)); // Prevents static entries from being grayed out

        val stonsby = new ArrayList<DiningMenu>(1);
        stonsby.add(new DiningMenu(getContext().getString(R.string.dining_stonsby_title), 0, dummyMeal));
        val newarkHalls = new SimpleSection<DiningMenu>(nwkCampusFullString, stonsby);

        val gateway = new ArrayList<DiningMenu>(1);
        gateway.add(new DiningMenu(getContext().getString(R.string.dining_gateway_title), 0, dummyMeal));
        val camdenHalls = new SimpleSection<DiningMenu>(camCampusFullString, gateway);

        try {
            val diningMenus = DiningAPI.getDiningHalls();
            val nbHalls = new SimpleSection<DiningMenu>(nbCampusFullString, diningMenus);

            // Determine campus ordering
            if (userHome.equals(nwkCampusFullString)) {
                simpleSections.add(newarkHalls);
                simpleSections.add(camdenHalls);
                simpleSections.add(nbHalls);
            } else if (userHome.equals(camCampusFullString)) {
                simpleSections.add(camdenHalls);
                simpleSections.add(newarkHalls);
                simpleSections.add(nbHalls);
            } else {
                simpleSections.add(nbHalls);
                simpleSections.add(camdenHalls);
                simpleSections.add(newarkHalls);
            }
        } catch (JsonSyntaxException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return simpleSections;
    }
}
