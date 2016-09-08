package edu.rutgers.css.Rutgers.api;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.channels.athletics.fragments.AthleticsDisplay;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusMain;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.channels.feedback.fragments.FeedbackMain;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMain;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodMeal;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesMain;
import edu.rutgers.css.Rutgers.channels.reader.fragments.RSSReader;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationDisplay;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationHoursDisplay;
import edu.rutgers.css.Rutgers.channels.recreation.fragments.RecreationMain;
import edu.rutgers.css.Rutgers.channels.ruinfo.fragments.RUInfoMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCCourses;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCSections;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.BookmarksDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Component builder
 */
public final class ComponentFactory {

    /** Log tag. */
    private static final String TAG                 = "ComponentFactory";

    /* Standard argument bundle tags */

    /** Handle of fragment to launch. */
    public static final String ARG_COMPONENT_TAG    = "component";

    /** Title to display in action bar. */
    public static final String ARG_TITLE_TAG        = "title";

    /** Channel handle, usually JSON-defined. */
    public static final String ARG_HANDLE_TAG       = "handle";

    /** Top handle in heirarchy. Currently used for DTables. */
    public static final String ARG_TOP_HANDLE_TAG       = "topHandle";

    /** JSON data passed as a string. */
    public static final String ARG_DATA_TAG         = "data";

    /** URL argument. */
    public static final String ARG_URL_TAG          = "url";

    /** RUMobile API file argument. */
    public static final String ARG_API_TAG          = "api";

    /** Count argument. For example, the number of events to grab in Events. */
    public static final String ARG_COUNT_TAG        = "count";

    public static final String ARG_HIST_TAG         = "history";

    public static final String ARG_BACKSTACK_TAG    = "backStack";

    /** Table of fragments that can be launched. Handles must be lowercase. */
    private static Map<String, Class<? extends Fragment>> sFragmentTable =
            Collections.unmodifiableMap(new HashMap<String, Class<? extends Fragment>>() {{
        // General views
        put(DTable.HANDLE, DTable.class);
        put(RSSReader.HANDLE, RSSReader.class);
        put(TextDisplay.HANDLE, TextDisplay.class);
        put(WebDisplay.HANDLE, WebDisplay.class);

        // Athletics views
        put(AthleticsDisplay.HANDLE, AthleticsDisplay.class);

        // Bus views
        put(BusMain.HANDLE, BusMain.class);
        put(BusDisplay.HANDLE, BusDisplay.class);

        // Food views
        put(FoodMain.HANDLE, FoodMain.class);
        put(FoodHall.HANDLE, FoodHall.class);
        put(FoodMeal.HANDLE, FoodMeal.class);

        // Places views
        put(PlacesMain.HANDLE, PlacesMain.class);
        put(PlacesDisplay.HANDLE, PlacesDisplay.class);

        // Recreation views
        put(RecreationMain.HANDLE, RecreationMain.class);
        put(RecreationDisplay.HANDLE, RecreationDisplay.class);
        put(RecreationHoursDisplay.HANDLE, RecreationHoursDisplay.class);

        // SOC views
        put(SOCMain.HANDLE, SOCMain.class);
        put(SOCCourses.HANDLE, SOCCourses.class);
        put(SOCSections.HANDLE, SOCSections.class);

        // Other views
        put(RUInfoMain.HANDLE, RUInfoMain.class);
        put(FeedbackMain.HANDLE, FeedbackMain.class);
        put(AboutDisplay.HANDLE, AboutDisplay.class);
        put(BookmarksDisplay.HANDLE, BookmarksDisplay.class);
    }});

    public ComponentFactory() {

    }
    
    /**
     * Create component fragment
     * @param args Argument bundle with at least 'component' argument set to describe which
     *                component to build. The options bundle will be passed to the new fragment.
     * @return Built fragment
     */
    public Fragment createFragment(@NonNull Bundle args) {
        Fragment fragment;
        String component;
        
        if (StringUtils.isBlank(args.getString(ARG_COMPONENT_TAG))) {
            throw new IllegalArgumentException("\"" + ARG_COMPONENT_TAG + "\" must be set");
        }
        
        component = args.getString(ARG_COMPONENT_TAG).toLowerCase(Locale.US);
        final Class<? extends Fragment> componentClass = sFragmentTable.get(component);
        if (componentClass != null) {
            LOGV(TAG, "Creating a " + componentClass.getSimpleName());
            try {
                fragment = componentClass.newInstance();
            } catch (Exception e) {
                LOGE(TAG, Log.getStackTraceString(e));
                return null;
            }    
        } else {
            LOGE(TAG, "Component \"" + component + "\" not found");
            return null;
        }

        final Bundle argsCopy = new Bundle(args);
        argsCopy.remove(ARG_COMPONENT_TAG);
        fragment.setArguments(argsCopy);

        return fragment;
    }

}
