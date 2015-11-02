package edu.rutgers.css.Rutgers.channels.bus.model.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusItem;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.StopStub;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Load bus route and stop information from Nextbus
 */
public class NextBusItemLoader extends SimpleAsyncLoader<List<SimpleSection<NextbusItem>>> {

    public static final String TAG = "NextBusItemLoader";

    public NextBusItemLoader(Context context) {
        super(context);
    }

    @Override
    public List<SimpleSection<NextbusItem>> loadInBackground() {
        List<SimpleSection<NextbusItem>> nextbusItems = new ArrayList<>();
        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getContext());
        final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

        try {
            // Get promises for all route & stop information
            final List<RouteStub> nbRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NB);
            final List<StopStub> nbStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NB);
            final List<RouteStub> nwkRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NWK);
            final List<StopStub> nwkStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NWK);

            if (nbHome) {
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NB, nbRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NB, nbStops));
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NWK, nwkStops));
            } else {
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NWK, nwkStops));
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NB, nbRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NB, nbStops));
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return nextbusItems;
    }

    private SimpleSection<NextbusItem> loadStops(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        // Get header for stops section
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_all_stops_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_all_stops_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        List<NextbusItem> nextbusItems = new ArrayList<>(stopStubs.size());
        nextbusItems.addAll(stopStubs);

        return new SimpleSection<>(header, nextbusItems);
    }

    private SimpleSection<NextbusItem> loadRoutes(@NonNull String agency, @NonNull List<RouteStub> routeStubs) {
        // Get header for routes section
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_all_routes_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_all_routes_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        List<NextbusItem> nextbusItems = new ArrayList<>(routeStubs.size());
        nextbusItems.addAll(routeStubs);

        return new SimpleSection<>(header, nextbusItems);
    }
}
