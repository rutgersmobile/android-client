package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Loads dining menus in a new thread
 */
public class DiningMenuSectionLoader extends AsyncTaskLoader<List<SimpleSection<DiningMenu>>> {

    Context context;

    public static final String TAG = "DiningMenuSectionLoader";

    private List<SimpleSection<DiningMenu>> mData;

    public DiningMenuSectionLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public List<SimpleSection<DiningMenu>> loadInBackground() {
        final List<SimpleSection<DiningMenu>> simpleSections = new ArrayList<>();
        // Get user's home campus
        final String userHome = RutgersUtils.getHomeCampus(context);

        // getString() in callback can cause crashes - load Resource strings here
        final String nbCampusFullString = context.getString(R.string.campus_nb_full);
        final String nwkCampusFullString = context.getString(R.string.campus_nwk_full);
        final String camCampusFullString = context.getString(R.string.campus_cam_full);

        // Static dining hall entries
        List<DiningMenu.Meal> dummyMeal = new ArrayList<>(1);
        dummyMeal.add(new DiningMenu.Meal("fake", true, null)); // Prevents static entries from being grayed out

        List<DiningMenu> stonsby = new ArrayList<>(1);
        stonsby.add(new DiningMenu(context.getString(R.string.dining_stonsby_title), 0, dummyMeal));
        final SimpleSection<DiningMenu> newarkHalls = new SimpleSection<>(nwkCampusFullString, stonsby);

        List<DiningMenu> gateway = new ArrayList<>(1);
        gateway.add(new DiningMenu(context.getString(R.string.dining_gateway_title), 0, dummyMeal));
        final SimpleSection<DiningMenu> camdenHalls = new SimpleSection<>(camCampusFullString, gateway);

        try {
            List<DiningMenu> diningMenus = DiningAPI.getDiningHalls();
            SimpleSection<DiningMenu> nbHalls = new SimpleSection<>(nbCampusFullString, diningMenus);

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
            AppUtils.showFailedLoadToast(context);
        }

        return simpleSections;
    }

    @Override
    public void deliverResult(List<SimpleSection<DiningMenu>> simpleSections) {
        if (isReset()) {
            return;
        }

        List<SimpleSection<DiningMenu>> oldItems = mData;
        mData = simpleSections;
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
