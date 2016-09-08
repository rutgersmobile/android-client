package edu.rutgers.css.Rutgers.channels.dtable.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableElement;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableLinearAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.channels.dtable.model.loader.DTableLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Dynamic Table
 * <p>Use {@link #dTag()} instead of TAG when logging</p>
 */
public class DTable extends DtableChannelFragment implements LoaderManager.LoaderCallbacks<DTableRoot> {

    /* Log tag and component handle */
    private static final String TAG                 = "DTable";
    public static final String HANDLE               = "dtable";
    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_HANDLE_TAG      = ComponentFactory.ARG_HANDLE_TAG;
    private static final String ARG_TOP_HANDLE_TAG  = "topHandle";
    private static final String ARG_API_TAG         = ComponentFactory.ARG_API_TAG;
    private static final String ARG_URL_TAG         = ComponentFactory.ARG_URL_TAG;
    private static final String ARG_DATA_TAG        = Config.PACKAGE_NAME + ".dtable.data";

    /* Saved instance state tags */
    private static final String SAVED_HANDLE_TAG    = Config.PACKAGE_NAME + ".dtable.saved.handle";
    private static final String SAVED_ROOT_TAG      = Config.PACKAGE_NAME + ".dtable.saved.root";

    /* Member data */
    private DTableRoot mDRoot;
    private DTableAdapter mAdapter;
    private String mURL;
    private String mAPI;
    private String mHandle;
    private String mTopHandle;
    private boolean mLoading;
    private String mTitle;

    public DTable() {
        // Required empty public constructor
    }

    /** Creates basic argument bundle - fields common to all bundles */
    private static Bundle baseArgs(@NonNull String title, @NonNull String handle, @NonNull String topHandle) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, DTable.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_HANDLE_TAG, handle);
        bundle.putString(ARG_TOP_HANDLE_TAG, topHandle);
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from a URL. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull URL url) {
        Bundle bundle = baseArgs(title, handle, topHandle);
        bundle.putString(ARG_URL_TAG, url.toString());
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from the RUMobile API. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull String api) {
        Bundle bundle = baseArgs(title, handle, topHandle);
        bundle.putString(ARG_API_TAG, api);
        return bundle;
    }

    /** Create argument bundle for a DTable that launches with pre-loaded table data. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull DTableRoot tableRoot) {
        Bundle bundle = baseArgs(title, handle, topHandle);
        bundle.putSerializable(ARG_DATA_TAG, tableRoot);
        return bundle;
    }

    /**
     * Get channel-specific DTable tag to use for logging.
     * @return DTable tag combined with current handle, if possible
     */
    private String dTag() {
        if (mHandle != null) return TAG + "_" + mHandle;
        else return TAG;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();

        // Get handle for this DTable instance
        final String handle = args.getString(ARG_HANDLE_TAG);
        final String api = args.getString(ARG_API_TAG);
        mTitle = args.getString(ARG_TITLE_TAG);
        if (handle != null) {
            mHandle = handle;
        } else if (api != null) {
            mHandle = api.replace(".txt","");
        } else if (mTitle != null) {
            mHandle = mTitle;
        } else {
            mHandle = "invalid";
        }

        mTopHandle = args.getString(ARG_TOP_HANDLE_TAG, mHandle);

        // If recreating, restore state
        if (savedInstanceState != null ) {
            mDRoot = (DTableRoot) savedInstanceState.getSerializable(SAVED_ROOT_TAG);
            if (mDRoot != null) {
                mHandle = savedInstanceState.getString(SAVED_HANDLE_TAG);
                createAdapter();
                LOGD(dTag(), "Restoring mData");
                return;
            }
        }

        try {
            final DTableRoot dataRoot = (DTableRoot) args.getSerializable(ARG_DATA_TAG);
            // If table data was provided in "data" field, load it
            if (dataRoot != null) {
                mDRoot = dataRoot;
                createAdapter();
                return;
            }
        } catch (ClassCastException e) {
            LOGE(dTag(), "onCreateView(): " + e.getMessage());
        }

        // Otherwise, check for URL or API to load table from
        if (mDRoot == null) {
            final String url = args.getString(ARG_URL_TAG);
            if (url != null) {
                mURL = url;
            } else if (api != null) {
                mAPI = api;
            } else {
                LOGE(dTag(), "DTable must have URL, API, or data in its arguments bundle");
                Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAdapter = new DTableLinearAdapter(
                new ArrayList<DTableElement>(),
                getFragmentMediator(),
                mHandle,
                mTopHandle,
                RutgersUtils.getHomeCampus(getContext()),
                new ArrayList<String>()
        );

        // Start loading DTableRoot object in another thread
        mLoading = true;
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void createAdapter() {
        mAdapter = new DTableLinearAdapter(
                mDRoot.getChildren(),
                getFragmentMediator(),
                mHandle,
                mTopHandle,
                RutgersUtils.getHomeCampus(getContext()),
                mDRoot.getHistory()
        );
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);

        if (mTitle != null) {
            getActivity().setTitle(mTitle);
        }

        if (mLoading) showProgressCircle();

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setItemAnimator(new FadeInAnimator());
        recyclerView.setAdapter((RecyclerView.Adapter) mAdapter);

        return v;
    }

    @Override
    public Link getLink() {
        final List<String> linkArgs = new ArrayList<>();
        for (final String title : mDRoot.getHistory()) {
            linkArgs.add(title);
        }

        return  new Link(mTopHandle, linkArgs, getLinkTitle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If any data was actually loaded, save it in state
        if (mDRoot != null) {
            outState.putSerializable(SAVED_ROOT_TAG, mDRoot);
            outState.putString(SAVED_HANDLE_TAG, mHandle);
        }
    }

    @Override
    public Loader<DTableRoot> onCreateLoader(int id, Bundle args) {
        return new DTableLoader(getContext(), mURL, mAPI, dTag());
    }

    @Override
    public void onLoadFinished(Loader<DTableRoot> loader, DTableRoot data) {
        reset();

        if (data == null) {
            AppUtils.showFailedLoadToast(getActivity());
            return;
        }

        // Data will always be returned as non-null unless there was an error
        mDRoot = data;
        mAdapter.addAll(data.getChildren());
        mAdapter.addAllHistory(data.getHistory());
    }

    private void reset() {
        mAdapter.clear();
        mAdapter.clearHistory();
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public void onLoaderReset(Loader<DTableRoot> loader) {
        reset();
    }
}