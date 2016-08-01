package edu.rutgers.css.Rutgers.channels.places.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.loader.PlaceLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Display information about a Rutgers location from the Places database.
 * @author James Chambers
 */
public class PlacesDisplay extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<PlaceLoader.PlaceHolder> {

    /* Log tag and component handle */
    private static final String TAG                 = "PlacesDisplay";
    public static final String HANDLE               = "placesdisplay";

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

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
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, new ArrayList<RMenuRow>());

        final Bundle args = getArguments();
        mTitle = args.getString(ARG_TITLE_TAG);

        // start loading place
        mLoading = true;
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.createView(inflater, container, savedInstanceState, R.layout.fragment_list_progress);

        if (mLoading) showProgressCircle();

        // Set title
        if (mTitle != null) getActivity().setTitle(mTitle);
        else getActivity().setTitle(R.string.places_title);

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final RMenuItemRow clicked = (RMenuItemRow) parent.getAdapter().getItem(position);

                switch (clicked.getArgs().getInt(ID_KEY)) {
                    case ADDRESS_ROW:
                        launchMap();
                        break;
                    case DESC_ROW:
                        final Bundle textArgs = TextDisplay.createArgs(mPlace.getTitle(), clicked.getArgs().getString("data"));
                        switchFragments(textArgs);
                        break;
                    case BUS_ROW:
                        final Bundle busArgs = new Bundle(clicked.getArgs());
                        busArgs.remove(ID_KEY);
                        switchFragments(busArgs);
                        break;
                }
            }
        });

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

    @Override
    public Loader<PlaceLoader.PlaceHolder> onCreateLoader(int id, Bundle args) {
        final String key = args.getString(ARG_PLACEKEY_TAG);
        return new PlaceLoader(getActivity(), key, ID_KEY, ADDRESS_ROW, DESC_ROW, BUS_ROW);
    }

    @Override
    public void onLoadFinished(Loader<PlaceLoader.PlaceHolder> loader, PlaceLoader.PlaceHolder data) {
        reset();

        // These fields will be non-null and non-empty on success
        if (data.getPlace() == null || data.getRows().isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }

        mTitle = data.getPlace().getTitle();
        mAdapter.addAll(data.getRows());
        mPlace = data.getPlace();
    }

    @Override
    public void onLoaderReset(Loader<PlaceLoader.PlaceHolder> loader) {
        reset();
    }

    private void reset() {
        hideProgressCircle();
        mLoading = false;
        mAdapter.clear();
    }
}
