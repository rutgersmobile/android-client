package edu.rutgers.css.Rutgers.channels.places.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.StopGroup;
import edu.rutgers.css.Rutgers.channels.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.PlacesAPI;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Display information about a Rutgers location from the Places database.
 * @author James Chambers
 */
public class PlacesDisplay extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "PlacesDisplay";
    public static final String HANDLE = "placesdisplay";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_PLACEKEY_TAG    = "placekey";

    /* Constants */
    private static final String ID_KEY = Config.PACKAGE_NAME+"."+HANDLE+".row.id";
    private static final int ADDRESS_ROW = 0;
    private static final int DESC_ROW = 1;
    private static final int BUS_ROW = 2;

    /* Member data */
    private Place mPlace;
    private List<RMenuRow> mData;
    private RMenuAdapter mAdapter;
    private boolean mLoading;

    /* View references */
    private ProgressBar mProgressCircle;

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

    public PlacesDisplay() {
        // Required empty public constructor
    }

    /** Create argument bundle for Rutgers place/building display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String placeKey) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, PlacesDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_PLACEKEY_TAG, placeKey);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new ArrayList<>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);

        final Bundle args = getArguments();

        // Get place key
        String key = args.getString(ARG_PLACEKEY_TAG);
        if(key == null) {
            AppUtils.showFailedLoadToast(getActivity());
            Log.e(TAG, ARG_PLACEKEY_TAG + " is null");
            return;
        }

        // Get resource strings
        final String addressHeader = getString(R.string.address_header);
        final String buildingNoHeader = getString(R.string.building_no_header);
        final String campusHeader = getString(R.string.campus_header);
        final String descriptionHeader = getString(R.string.description_header);
        final String officesHeader = getString(R.string.offices_header);
        final String nearbyHeader = getString(R.string.nearby_bus_header);

        // Keep track of whether place data is loading
        mLoading = true;

        final AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(PlacesAPI.getPlace(key)).done(new DoneCallback<Place>() {
            @Override
            public void onDone(Place result) {
                mPlace = result;

                // Add address rows
                if (mPlace.getLocation() != null) {

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
                        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            height = width / 4;
                        } else {
                            height = width / 2;
                        }

                        URL imgUrl = new URL("https://maps.googleapis.com/maps/api/staticmap?zoom=18&size=" + (width / 2) + "x" + (height / 2) + "&markers=size:mid|color:red|" + mPlace.getLocation().getLatitude() + "," + mPlace.getLocation().getLongitude());
                        RMenuImageRow staticMapRow = new RMenuImageRow(imgUrl, width, height);
                        mAdapter.add(staticMapRow);
                    } catch (MalformedURLException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    */

                    Bundle addressArgs = new Bundle();
                    addressArgs.putInt(ID_KEY, ADDRESS_ROW);
                    addressArgs.putString("title", formatAddress(mPlace.getLocation()));
                    mAdapter.add(new RMenuHeaderRow(addressHeader));
                    mAdapter.add(new RMenuItemRow(addressArgs));
                }

                // Add description row
                if (!StringUtils.isEmpty(mPlace.getDescription())) {
                    Bundle descArgs = new Bundle();
                    descArgs.putInt(ID_KEY, DESC_ROW);
                    descArgs.putString("title", StringUtils.abbreviate(mPlace.getDescription(), 80));
                    descArgs.putString("data", mPlace.getDescription());
                    mAdapter.add(new RMenuHeaderRow(descriptionHeader));
                    mAdapter.add(new RMenuItemRow(descArgs));
                }

                // Add nearby bus stops
                if (mPlace.getLocation() != null) {
                    final int startPos = mAdapter.getCount();

                    Place.Location location = mPlace.getLocation();
                    double buildLat = location.getLatitude();
                    double buildLon = location.getLongitude();

                    // Determine Nextbus agency by campus
                    final String agency = sAgencyMap.get(mPlace.getCampusName());

                    if (agency != null) {
                        dm.when(NextbusAPI.getStopsByTitleNear(agency, buildLat, buildLon)).then(new DoneCallback<List<StopGroup>>() {
                            @Override
                            public void onDone(List<StopGroup> result) {
                                if (!result.isEmpty()) {
                                    // There are nearby stops. Add header and all stops.
                                    int insertPos = startPos;
                                    mData.add(insertPos++, new RMenuHeaderRow(nearbyHeader));
                                    mAdapter.notifyDataSetChanged();

                                    for (StopGroup stopGroup : result) {
                                        Bundle stopArgs = BusDisplay.createArgs(stopGroup.getTitle(), BusDisplay.STOP_MODE, agency, stopGroup.getTitle());
                                        stopArgs.putInt(ID_KEY, BUS_ROW);
                                        mData.add(insertPos++, new RMenuItemRow(stopArgs));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }).fail(new FailCallback<Exception>() {
                            @Override
                            public void onFail(Exception e) {
                                Log.w(TAG, "Getting nearby buses failed: " + e.getMessage());
                            }
                        });
                    }
                }

                // Add offices housed in this building
                if (mPlace.getOffices() != null) {
                    mAdapter.add(new RMenuHeaderRow(officesHeader));
                    for (String office : mPlace.getOffices()) {
                        mAdapter.add(new RMenuItemRow(office));
                    }
                }

                // Add building number row
                if (!StringUtils.isEmpty(mPlace.getBuildingNumber())) {
                    mAdapter.add(new RMenuHeaderRow(buildingNoHeader));
                    mAdapter.add(new RMenuItemRow(mPlace.getBuildingNumber()));
                }

                // Add campus rows
                if (!StringUtils.isEmpty(mPlace.getCampusName())) {
                    mAdapter.add(new RMenuHeaderRow(campusHeader));
                    mAdapter.add(new RMenuItemRow(mPlace.getCampusName()));
                }
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                AppUtils.showFailedLoadToast(getActivity());
                Log.w(TAG, e.getMessage());
            }
        }).always(new AlwaysCallback<Place, Exception>() {
            @Override
            public void onAlways(Promise.State state, Place resolved, Exception rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_place_display, container, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if(mLoading) showProgressCircle();

        // Set title
        final Bundle args = getArguments();
        if(args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.places_title);

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getAdapter().getItem(position);

                switch(clicked.getArgs().getInt(ID_KEY)) {
                    case ADDRESS_ROW:
                        launchMap();
                        break;
                    case DESC_ROW:
                        Bundle textArgs = TextDisplay.createArgs(mPlace.getTitle(), clicked.getArgs().getString("data"));
                        ComponentFactory.getInstance().switchFragments(textArgs);
                        break;
                    case BUS_ROW:
                        Bundle busArgs = new Bundle(clicked.getArgs());
                        busArgs.remove(ID_KEY);
                        ComponentFactory.getInstance().switchFragments(busArgs);
                        break;
                }
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    /**
     * Start a map activity intent for this address/location
     */
    private void launchMap() {
        if(mPlace == null || mPlace.getLocation() == null) return;
        Place.Location location = mPlace.getLocation();

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Create the maps query. Prefer addresses for user readability.
        if(!StringUtils.isEmpty(location.getStreet()) && !StringUtils.isEmpty(location.getCity()) && !StringUtils.isEmpty(location.getStateAbbr())) {
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

    /**
     * Compile location information into readable string form
     * @param location Place location info
     * @return Multi-line string containing address
     */
    private static String formatAddress(Place.Location location) {
        if(location == null) return null;

        String resultString = "";
        if(!StringUtils.isEmpty(location.getName())) resultString += location.getName() + "\n";
        if(!StringUtils.isEmpty(location.getStreet())) resultString += location.getStreet() + "\n";
        if(!StringUtils.isEmpty(location.getAdditional())) resultString += location.getAdditional() + "\n";
        if(!StringUtils.isEmpty(location.getCity())) resultString += location.getCity() + ", " +
                location.getStateAbbr() + " " + location.getPostalCode();

        return StringUtils.trim(resultString);
    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
