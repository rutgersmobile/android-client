package edu.rutgers.css.Rutgers.channels.places.model.loader;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.StopGroup;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.channels.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.PlacesAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import lombok.Data;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Loads places
 */
public class PlaceLoader extends SimpleAsyncLoader<PlaceLoader.PlaceHolder> {
    private String key;
    private String idKey;
    private int addressRow;
    private int descRow;
    private int busRow;

    private String addressHeader;
    private String buildingNoHeader;
    private String campusHeader;
    private String descriptionHeader;
    private String officesHeader;
    private String nearbyHeader;

    public static final String TAG = "PlaceLoader";

    // Maps campuses to Nextbus agencies. Used for listing nearby bus stops.
    private static final Map<String, String> sAgencyMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Busch", NextbusAPI.AGENCY_NB);
        put("College Avenue", NextbusAPI.AGENCY_NB);
        put("Douglass", NextbusAPI.AGENCY_NB);
        put("Cook", NextbusAPI.AGENCY_NB);
        put("Livingston", NextbusAPI.AGENCY_NB);
        put("Newark", NextbusAPI.AGENCY_NWK);
        put("Health Sciences at Newark", NextbusAPI.AGENCY_NWK);
    }});

    @Data
    public class PlaceHolder {
        final List<RMenuRow> rows;
        final Place place;
    }

    public PlaceLoader(Context context, String key, String idKey, int addressRow, int descRow, int busRow) {
        super(context);
        this.key = key;
        this.idKey = idKey;
        this.addressRow = addressRow;
        this.descRow = descRow;
        this.busRow = busRow;

        addressHeader = context.getString(R.string.address_header);
        buildingNoHeader = context.getString(R.string.building_no_header);
        campusHeader = context.getString(R.string.campus_header);
        descriptionHeader = context.getString(R.string.description_header);
        officesHeader = context.getString(R.string.offices_header);
        nearbyHeader = context.getString(R.string.nearby_bus_header);
    }

    @Override
    public PlaceHolder loadInBackground() {
        // Get place key
        if (key == null) {
            AppUtils.showFailedLoadToast(getContext());
            LOGE(TAG, PlacesDisplay.ARG_PLACEKEY_TAG + " is null");
            return null;
        }

        final List<RMenuRow> rows = new ArrayList<>();
        Place place = null;

        try {
            place = PlacesAPI.getPlace(key);
            // Add address rows
            if (place.getLocation() != null) {

                // Map deferred to a later release
                /*
                // Get static map image and add it
                try {
                    WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    // Note: The maximum dimensions for free requests is 640x640
                    int width = size.x;
                    int height;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        height = width / 4;
                    } else {
                        height = width / 2;
                    }

                    URL imgUrl = new URL("https://maps.googleapis.com/maps/api/staticmap?zoom=18&size=" + (width / 2) + "x" + (height / 2) + "&markers=size:mid|color:red|" + mPlace.getLocation().getLatitude() + "," + mPlace.getLocation().getLongitude());
                    RMenuImageRow staticMapRow = new RMenuImageRow(imgUrl, width, height);
                    mAdapter.add(staticMapRow);
                } catch (MalformedURLException e) {
                    LOGE(TAG, e.getMessage());
                }
                */

                final Bundle addressArgs = new Bundle();
                addressArgs.putInt(idKey, addressRow);
                addressArgs.putString("title", formatAddress(place.getLocation()));
                rows.add(new RMenuHeaderRow(addressHeader));
                rows.add(new RMenuItemRow(addressArgs));
            }

            // Add description row
            if (!StringUtils.isEmpty(place.getDescription())) {
                final Bundle descArgs = new Bundle();
                descArgs.putInt(idKey, descRow);
                descArgs.putString("title", StringUtils.abbreviate(place.getDescription(), 80));
                descArgs.putString("data", place.getDescription());
                rows.add(new RMenuHeaderRow(descriptionHeader));
                rows.add(new RMenuItemRow(descArgs));
            }

            // Add nearby bus stops
            if (place.getLocation() != null) {
                final int startPos = rows.size();

                final Place.Location location = place.getLocation();
                final double buildLat = location.getLatitude();
                final double buildLon = location.getLongitude();

                // Determine Nextbus agency by campus
                final String agency = sAgencyMap.get(place.getCampusName());

                if (agency != null) {
                    final List<StopGroup> stops = NextbusAPI.getStopsByTitleNear(agency, buildLat, buildLon);
                    if (!stops.isEmpty()) {
                        // There are nearby stops. Add header and all stops.
                        int insertPos = startPos;
                        rows.add(insertPos++, new RMenuHeaderRow(nearbyHeader));

                        for (final StopGroup stopGroup : stops) {
                            final Bundle stopArgs = BusDisplay.createArgs(stopGroup.getTitle(), BusDisplay.STOP_MODE, agency, stopGroup.getTitle());
                            stopArgs.putInt(idKey, busRow);
                            rows.add(insertPos++, new RMenuItemRow(stopArgs));
                        }
                    }
                }
            }

            // Add offices housed in this building
            if (place.getOffices() != null) {
                rows.add(new RMenuHeaderRow(officesHeader));
                for (final String office : place.getOffices()) {
                    rows.add(new RMenuItemRow(office));
                }
            }

            // Add building number row
            if (!StringUtils.isEmpty(place.getBuildingNumber())) {
                rows.add(new RMenuHeaderRow(buildingNoHeader));
                rows.add(new RMenuItemRow(place.getBuildingNumber()));
            }

            // Add campus rows
            if (!StringUtils.isEmpty(place.getCampusName())) {
                rows.add(new RMenuHeaderRow(campusHeader));
                rows.add(new RMenuItemRow(place.getCampusName()));
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return new PlaceHolder(rows, place);
    }

    /**
     * Compile location information into readable string form
     * @param location Place location info
     * @return Multi-line string containing address
     */
    private static String formatAddress(Place.Location location) {
        if (location == null) return null;

        String resultString = "";
        if (!StringUtils.isEmpty(location.getName())) resultString += location.getName() + "\n";
        if (!StringUtils.isEmpty(location.getStreet())) resultString += location.getStreet() + "\n";
        if (!StringUtils.isEmpty(location.getAdditional())) resultString += location.getAdditional() + "\n";
        if (!StringUtils.isEmpty(location.getCity())) resultString += location.getCity() + ", " +
                location.getStateAbbr() + " " + location.getPostalCode();

        return StringUtils.trim(resultString);
    }
}
