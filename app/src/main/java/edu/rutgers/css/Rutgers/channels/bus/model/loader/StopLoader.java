package edu.rutgers.css.Rutgers.channels.bus.model.loader;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.bus.NextbusAPI;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for bus stops
 */
public class StopLoader extends SimpleAsyncLoader<List<SimpleSection<StopStub>>> {
    public static final String TAG = "StopLoader";
    private Location location;

    public StopLoader(Context context, Location location) {
        super(context);
        this.location = location;
    }

    @Override
    public List<SimpleSection<StopStub>> loadInBackground() {
        // Stops is the return value it will hold headers as well as stops
        final List<SimpleSection<StopStub>> stops = new ArrayList<>();
        // Nearby stops are one of the sections in stops
        final List<StopStub> nearbyStops = new ArrayList<>();
        stops.add(new SimpleSection<>(getContext().getString(R.string.nearby_bus_header), nearbyStops));

        // Check for location services
        if (location != null) {
            if (BuildConfig.DEBUG) LOGD(TAG, "Current location: " + location.toString());
            LOGI(TAG, "Updating nearby active stops");

            try {
                // Get nearby stops from the api
                final List<StopGroup> nbNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NB, (float) location.getLatitude(), (float) location.getLongitude());
                final List<StopGroup> nwkNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NWK, (float) location.getLatitude(), (float) location.getLongitude());
                if (nbNearbyStops.isEmpty() && nwkNearbyStops.isEmpty()) {
                    // If there aren't any results, put a "no stops nearby" message
                    //addNearbyRow(1, new RMenuItemRow(noneNearbyString));
                } else {
                    // Add all the stops
                    for (StopGroup stopGroup : nbNearbyStops)
                        nearbyStops.add(new StopStub(stopGroup));
                    for (StopGroup stopGroup : nwkNearbyStops)
                        nearbyStops.add(new StopStub(stopGroup));
                }
            } catch (JsonSyntaxException | IOException e) {
                LOGE(TAG, e.getMessage());
            }
        } else {
            LOGW(TAG, "Couldn't get location, can't find nearby stops");
            //addNearbyRow(1, new RMenuItemRow(getString(R.string.failed_location)));
        }

        // Get home campus for result ordering
        final String userHome = RutgersUtils.getHomeCampus(getContext());
        final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

        try {
            // Get active stops
            final List<StopStub> nbActiveStops = NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NB);
            final List<StopStub> nwkActiveStops = NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NWK);

            if (nbHome) {
                stops.add(loadAgency(NextbusAPI.AGENCY_NB, nbActiveStops));
                stops.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActiveStops));
            } else {
                stops.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActiveStops));
                stops.add(loadAgency(NextbusAPI.AGENCY_NB, nbActiveStops));
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }
        return stops;
    }

    /**
     * Populate list with bus stops for agency, with a section header for that agency.
     * @param agency Agency tag
     * @param stopStubs List of stop stubs (titles/geohashes) for that agency
     */
    private SimpleSection<StopStub> loadAgency(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_active_stops_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_active_stops_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        return new SimpleSection<>(header, stopStubs);
    }
}
