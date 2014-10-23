package edu.rutgers.css.Rutgers.fragments.Places;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers.fragments.Bus.BusDisplay;
import edu.rutgers.css.Rutgers.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.items.Place;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Display information about a Rutgers location from the Places database.
 * @author James Chambers
 */
public class PlacesDisplay extends Fragment {

	private static final String TAG = "PlacesDisplay";
    public static final String HANDLE = "placesdisplay";

    private static final String ID_KEY = "id";
    private static final int ADDRESS_ROW = 0;
    private static final int DESC_ROW = 1;
    private static final int BUS_ROW = 2;

    private Place mPlace;
    private List<RMenuRow> mData;
    private RMenuAdapter mAdapter;

    private static final Map<String, String> sAgencyMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Busch", "nb");
        put("College Avenue", "nb");
        put("Douglass", "nb");
        put("Cook", "nb");
        put("Livingston", "nb");
        put("Newark", "nwk");
        put("Health Sciences at Newark", "nwk");
    }});

    public PlacesDisplay() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mData = new ArrayList<RMenuRow>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);

        Bundle args = getArguments();

        // Get place key
        String key = args.getString("placeKey");
        if(key == null) {
            AppUtil.showFailedLoadToast(getActivity());
            Log.e(TAG, "placeKey is null");
            return;
        }

        // Get resource strings
        final String addressHeader = getString(R.string.address_header);
        final String buildingNoHeader = getString(R.string.building_no_header);
        final String campusHeader = getString(R.string.campus_header);
        final String descriptionHeader = getString(R.string.description_header);
        final String officesHeader = getString(R.string.offices_header);
        final String nearbyHeader = getString(R.string.nearby_bus_header);

        final AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Places.getPlace(key)).done(new DoneCallback<Place>() {
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
                    descArgs.putString("component", TextDisplay.HANDLE);
                    descArgs.putString("data", mPlace.getDescription());
                    mAdapter.add(new RMenuHeaderRow(descriptionHeader));
                    mAdapter.add(new RMenuItemRow(descArgs));
                }

                // Add nearby bus stops
                if (mPlace.getLocation() != null) {
                    final int startPos = mAdapter.getCount();

                    Place.Location location = mPlace.getLocation();
                    double buildLon = location.getLongitude();
                    double buildLat = location.getLatitude();

                    // Determine Nextbus agency by campus
                    final String agency = sAgencyMap.get(mPlace.getCampusName());

                    if (agency != null) {
                        dm.when(Nextbus.getStopsByTitleNear(agency, buildLat, buildLon)).then(new DoneCallback<JSONObject>() {
                            @Override
                            public void onDone(JSONObject result) {
                                if (result.length() > 0) {
                                    int insertPos = startPos;
                                    mData.add(insertPos++, new RMenuHeaderRow(nearbyHeader));
                                    mAdapter.notifyDataSetChanged();

                                    for (Iterator<String> stopTitleIter = result.keys(); stopTitleIter.hasNext(); ) {
                                        String title = stopTitleIter.next();
                                        Bundle stopArgs = new Bundle();
                                        stopArgs.putInt(ID_KEY, BUS_ROW);
                                        stopArgs.putString("title", title);
                                        stopArgs.putString("component", BusDisplay.HANDLE);
                                        stopArgs.putString("agency", agency);
                                        stopArgs.putString("mode", "stop");
                                        mData.add(insertPos++, new RMenuItemRow(stopArgs));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }).fail(new FailCallback<Exception>() {
                            @Override
                            public void onFail(Exception result) {
                                Log.w(TAG, "Getting nearby buses: " + result.getMessage());
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
                AppUtil.showFailedLoadToast(getActivity());
                Log.w(TAG, e.getMessage());
            }
        });

    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_place_display, container, false);
		Bundle args = getArguments();

        // Set title
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.places_title);

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getAdapter().getItem(position);
                Bundle newArgs = new Bundle(clicked.getArgs());

                switch(newArgs.getInt(ID_KEY)) {
                    case ADDRESS_ROW:
                        launchMap();
                        break;
                    case DESC_ROW:
                        newArgs.putString("title", mPlace.getTitle());
                        ComponentFactory.getInstance().switchFragments(newArgs);
                        break;
                    case BUS_ROW:
                        ComponentFactory.getInstance().switchFragments(newArgs);
                        break;
                }
            }
        });

		return v;
	}

    /**
     * Start a map activity intent for this address/location
     */
    public void launchMap() {
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

}
