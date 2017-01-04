package edu.rutgers.css.Rutgers.channels.places.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.RutgersAPI;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.model.places.Place;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Display information about a Rutgers location from the Places database.
 * @author James Chambers
 */
public class PlacesDisplay extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "PlacesDisplay";
    public static final String HANDLE               = "placesdisplay";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    public static final String ARG_PLACEKEY_TAG    = "placekey";

    /* Constants */
    private static final String ID_KEY = Config.PACKAGE_NAME+"."+HANDLE+".row.id";
    private static final int ADDRESS_ROW = 0;
    private static final int DESC_ROW = 1;
    private static final int BUS_ROW = 2;

    /* Member data */
    private Place mPlace;
    private RMenuAdapter mAdapter;
    private boolean mLoading;
    private String mTitle;

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

    public static class PlaceHolder {
        private final List<RMenuRow> rows;
        private final Place place;

        public PlaceHolder(final List<RMenuRow> rows, final Place place) {
            this.rows = rows;
            this.place = place;
        }

        public List<RMenuRow> getRows() {
            return rows;
        }

        public Place getPlace() {
            return place;
        }
    }

    public PlacesDisplay() {
        // Required empty public constructor
    }

    /** Create argument bundle for Rutgers place/building display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String placeKey) {
        final Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, PlacesDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_PLACEKEY_TAG, placeKey);
        return bundle;
    }

    /** Create argument bundle for Rutgers place/building display. */
    public static Bundle createArgs(@NonNull String placeKey) {
        final Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, PlacesDisplay.HANDLE);
        bundle.putString(ARG_PLACEKEY_TAG, placeKey);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, new ArrayList<>());
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdapter.getPositionClicks()
            .map(rMenuRow -> (RMenuItemRow) rMenuRow)
            .subscribe(rMenuItemRow -> {
                switch (rMenuItemRow.getArgs().getInt(ID_KEY)) {
                    case ADDRESS_ROW:
                        launchMap();
                        break;
                    case DESC_ROW:
                        final Bundle textArgs = TextDisplay.createArgs(
                            mPlace.getTitle(),
                            rMenuItemRow.getArgs().getString("data")
                        );
                        switchFragments(textArgs);
                        break;
                    case BUS_ROW:
                        final Bundle busArgs = new Bundle(rMenuItemRow.getArgs());
                        busArgs.remove(ID_KEY);
                        switchFragments(busArgs);
                        break;
                }
            }, this::logError);

        final Bundle args = getArguments();
        mTitle = args.getString(ARG_TITLE_TAG);

        // start loading place
        mLoading = true;
        final String addressHeader = getContext().getString(R.string.address_header);
        final String buildingNoHeader = getContext().getString(R.string.building_no_header);
        final String campusHeader = getContext().getString(R.string.campus_header);
        final String descriptionHeader = getContext().getString(R.string.description_header);
        final String officesHeader = getContext().getString(R.string.offices_header);
        final String nearbyHeader = getContext().getString(R.string.nearby_bus_header);

        final String placeKey = args.getString(ARG_PLACEKEY_TAG);

        RutgersAPI.getPlace(placeKey)
            .flatMap(place -> getStopsNearPlace(place).map(stopGroups -> {
                final List<RMenuRow> rows = new ArrayList<>();
                if (place.getLocation() != null) {
                    final Bundle addressArgs = new Bundle();
                    addressArgs.putInt(ID_KEY, ADDRESS_ROW);
                    addressArgs.putString("title", formatAddress(place.getLocation()));
                    rows.add(new RMenuHeaderRow(addressHeader));
                    rows.add(new RMenuItemRow(addressArgs));
                }

                if (!StringUtils.isEmpty(place.getDescription())) {
                    final Bundle descArgs = new Bundle();
                    descArgs.putInt(ID_KEY, DESC_ROW);
                    descArgs.putString("title", StringUtils.abbreviate(place.getDescription(), 80));
                    descArgs.putString("data", place.getDescription());
                    rows.add(new RMenuHeaderRow(descriptionHeader));
                    rows.add(new RMenuItemRow(descArgs));
                }

                if (!stopGroups.isEmpty()) {
                    int insertPos = rows.size();
                    rows.add(insertPos++, new RMenuHeaderRow(nearbyHeader));

                    final String agency = sAgencyMap.get(place.getCampusName());

                    for (final StopGroup stopGroup : stopGroups) {
                        final Bundle stopArgs = BusDisplay.createArgs(stopGroup.getTitle(), BusDisplay.STOP_MODE, agency, stopGroup.getTitle());
                        stopArgs.putInt(ID_KEY, BUS_ROW);
                        rows.add(insertPos++, new RMenuItemRow(stopArgs));
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

                return new PlaceHolder(rows, place);
            }))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(placeHolder -> {
                reset();
                mTitle = placeHolder.getPlace().getTitle();
                mAdapter.addAll(placeHolder.getRows());
                mPlace = placeHolder.getPlace();
            }, error -> {
                reset();
                logError(error);
            });
    }

    private static Observable<List<StopGroup>> getStopsNearPlace(final Place place) {
        if (place.getLocation() != null) {
            final Place.Location location = place.getLocation();
            final double buildLat = location.getLatitude();
            final double buildLon = location.getLongitude();

            // Determine Nextbus agency by campus
            final String agency = sAgencyMap.get(place.getCampusName());

            if (agency != null) {
                return NextbusAPI.getStopsByTitleNear(agency, buildLat, buildLon, Config.NEARBY_RANGE);
            }
        }

        return Observable.just(new ArrayList<>());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.createView(inflater, container, savedInstanceState, R.layout.fragment_recycler_progress);

        if (mLoading) showProgressCircle();

        // Set title
        if (mTitle != null) getActivity().setTitle(mTitle);
        else getActivity().setTitle(R.string.places_title);

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    public Link getLink() {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add(getArguments().getString(ARG_PLACEKEY_TAG));
        return new Link("places", pathParts, getLinkTitle());
    }

    /**
     * Start a map activity intent for this address/location
     */
    private void launchMap() {
        if (mPlace == null || mPlace.getLocation() == null) return;
        final Place.Location location = mPlace.getLocation();

        final Intent intent = new Intent(Intent.ACTION_VIEW);

        // Create the maps query. Prefer addresses for user readability.
        if (!StringUtils.isEmpty(location.getStreet())
                && !StringUtils.isEmpty(location.getCity())
                && !StringUtils.isEmpty(location.getStateAbbr())) {
            intent.setData(Uri.parse("geo:0,0?q=" + location.getStreet() + ", " + location.getCity() + ", " + location.getStateAbbr()));
        } else {
            intent.setData(Uri.parse("geo:0,0?q=" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude())));
        }

        // Try to launch a map activity
        try {
            startActivity(intent);
        }  catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
        }
    }

    private void reset() {
        hideProgressCircle();
        mLoading = false;
        mAdapter.clear();
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
