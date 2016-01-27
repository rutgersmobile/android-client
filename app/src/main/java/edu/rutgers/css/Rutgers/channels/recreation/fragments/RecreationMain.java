package edu.rutgers.css.Rutgers.channels.recreation.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.recreation.model.Campus;
import edu.rutgers.css.Rutgers.channels.recreation.model.Facility;
import edu.rutgers.css.Rutgers.channels.recreation.model.FacilityAdapter;
import edu.rutgers.css.Rutgers.channels.recreation.model.loader.CampusLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Display gyms and facilities for each campus
 */
public class RecreationMain extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<List<Campus>> {

    /* Log tag and component handle */
    private static final String TAG                 = "RecreationMain";
    public static final String HANDLE               = "recreation";
    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Argument bundle tags */
    public static final String ARG_TITLE_TAG        = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private FacilityAdapter mAdapter;
    private boolean mLoading;

    public RecreationMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main gyms screen. */
    public static Bundle createArgs(@NonNull String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RecreationMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FacilityAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);

        mLoading = true;
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_stickylist_progress);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.rec_title);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Facility facility = mAdapter.getItem(position);
                Campus campus = mAdapter.getSectionContainingItem(position);

                Bundle newArgs = RecreationDisplay.createArgs(facility.getTitle(),
                        campus.getTitle(), facility.getTitle());
                switchFragments(newArgs);
            }

        });
        
        return v;
    }

    @Override
    public Loader<List<Campus>> onCreateLoader(int id, Bundle args) {
        return new CampusLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<Campus>> loader, List<Campus> data) {
        // assume that an empty response is an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
        mAdapter.clear();
        mAdapter.addAll(data);
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public void onLoaderReset(Loader<List<Campus>> loader) {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public Link getLink() {
        return null;
    }

    @Override
    public ShareActionProvider getShareActionProvider() {
        return null;
    }
}
