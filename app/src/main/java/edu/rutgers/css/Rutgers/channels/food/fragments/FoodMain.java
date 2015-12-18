package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.loader.DiningMenuSectionLoader;
import edu.rutgers.css.Rutgers.channels.food.model.SchoolFacilitiesAdapter;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Displays dining halls that have menus available in the Dining API.
 * @author James Chambers
 */
public class FoodMain extends BaseChannelFragment
    implements LoaderManager.LoaderCallbacks<List<SimpleSection<DiningMenu>>> {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodMain";
    public static final String HANDLE               = "food";

    private static final int LOADER_ID              = 101;


    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private SchoolFacilitiesAdapter mAdapter;
    private boolean mLoading;
    private ShareActionProvider shareActionProvider;

    public FoodMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for dining hall listing. */
    public static Bundle createArgs(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new SchoolFacilitiesAdapter(getActivity(),
                R.layout.row_title, R.layout.row_section_header, R.id.title);

        // start loading dining menus
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_stickylist_progress);

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.dining_title);

        StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiningMenu clickedMenu = (DiningMenu) parent.getAdapter().getItem(position);

                // Launch text display if static hall is clicked. Otherwise, regular menu display.
                if (clickedMenu.getLocationName().equals(getString(R.string.dining_stonsby_title))) {
                    switchFragments(TextDisplay.createArgs(getString(R.string.dining_stonsby_title),
                            getString(R.string.dining_stonsby_description)));
                } else if (clickedMenu.getLocationName().equals(getString(R.string.dining_gateway_title))) {
                    switchFragments(TextDisplay.createArgs(getString(R.string.dining_gateway_title),
                            getString(R.string.dining_gateway_description)));
                } else {
                    if (clickedMenu.hasActiveMeals()) {
                        switchFragments(FoodHall.createArgs(clickedMenu.getLocationName()));
                    }
                }
            }
        });
        
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_link, menu);
        MenuItem shareItem = menu.findItem(R.id.deep_link_share);
        if (shareItem != null) {
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "http://rumobile.rutgers.edu/link/food/");
            shareActionProvider.setShareIntent(intent);
        }
    }

    @Override
    public Loader<List<SimpleSection<DiningMenu>>> onCreateLoader(int id, Bundle args) {
        mLoading = true;
        showProgressCircle();
        return new DiningMenuSectionLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleSection<DiningMenu>>> loader, List<SimpleSection<DiningMenu>> data) {
        // Assume an empty response is an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
        mAdapter.clear();
        mAdapter.addAll(data);
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<DiningMenu>>> loader) {
        mAdapter.clear();
    }
}
