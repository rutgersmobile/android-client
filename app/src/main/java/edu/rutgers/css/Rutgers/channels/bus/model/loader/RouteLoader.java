package edu.rutgers.css.Rutgers.channels.bus.model.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Load a list of routes
 */
public class RouteLoader extends SimpleAsyncLoader<List<SimpleSection<RouteStub>>> {

    public static final String TAG = "RouteLoader";

    public RouteLoader(Context context) {
        super(context);
    }

    @Override
    public List<SimpleSection<RouteStub>> loadInBackground() {
        final List<SimpleSection<RouteStub>> routes = new ArrayList<>();

        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getContext());
        final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

        try {
            // Get promises for active routes
            final List<RouteStub> nbActiveRoutes = NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NB);
            final List<RouteStub> nwkActiveRoutes = NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NWK);

            if (nbHome) {
                routes.add(loadAgency(NextbusAPI.AGENCY_NB, nbActiveRoutes));
                routes.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActiveRoutes));
            } else {
                routes.add(loadAgency(NextbusAPI.AGENCY_NWK, nbActiveRoutes));
                routes.add(loadAgency(NextbusAPI.AGENCY_NB, nwkActiveRoutes));
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return routes;
    }

    /**
     * Populate list with bus routes for agency, with a section header for that agency
     * @param agencyTag Agency tag for API request
     * @param routeStubs List of routes (stubs)
     */
    private SimpleSection<RouteStub> loadAgency(@NonNull String agencyTag, @NonNull List<RouteStub> routeStubs) {
        // Get header for routes section
        String header;
        switch (agencyTag) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_active_routes_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_active_routes_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agencyTag + "\"");
        }

        return new SimpleSection<>(header, routeStubs);

        /*
        if (routeStubs.isEmpty()) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_active_routes)));
        }
        */
    }
}
