package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusItem;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.NextBusItemLoader;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BusAll extends BaseChannelFragment
    implements LoaderManager.LoaderCallbacks<List<SimpleSection<NextbusItem>>> {

    /* Log tag and component handle */
    private static final String TAG                 = "BusAll";
    public static final String HANDLE               = "busall";

    /* ID for loader */
    private static final int LOADER_ID              = 1;

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = Config.PACKAGE_NAME+"."+HANDLE+".filter";

    /* Member data */
    private SimpleSectionedAdapter<NextbusItem> mAdapter;
    private String mFilterString;
    private boolean mLoading;

    public BusAll() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);

        // Restore filter
        if (savedInstanceState != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG);
        }

        // Start loading all stops and routes in the background
        mLoading = true;
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_stickylist_progress_simple);

        if (mLoading) showProgressCircle();

        // Set up list to accept clicks on route or stop rows
        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NextbusItem clickedItem = (NextbusItem) parent.getAdapter().getItem(position);
                String mode = clickedItem.getClass() == RouteStub.class ?
                        BusDisplay.ROUTE_MODE : BusDisplay.STOP_MODE;

                switchFragments(BusDisplay.createArgs(clickedItem.getTitle(), mode,
                        clickedItem.getAgencyTag(), clickedItem.getTag()));
            }

        });

        // Set main bus fragment as focus listener, for giving focus to search field
        BusMain mainFragment = (BusMain) getParentFragment();
        mainFragment.addSearchListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set filter for list adapter
                mFilterString = s.toString().trim();
                mAdapter.getFilter().filter(mFilterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (StringUtils.isNotBlank(mFilterString)) outState.putString(SAVED_FILTER_TAG, mFilterString);
    }

    @Override
    public Loader<List<SimpleSection<NextbusItem>>> onCreateLoader(int id, Bundle args) {
        return new NextBusItemLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleSection<NextbusItem>>> loader, List<SimpleSection<NextbusItem>> data) {
        mAdapter.clear();
        mAdapter.addAll(data);
        mLoading = false;
        hideProgressCircle();

        // If we get nothing back assume it's an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }

        // Set filter after info is re-loaded
        if (mFilterString != null) {
            mAdapter.getFilter().filter(mFilterString);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<NextbusItem>>> loader) {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}
