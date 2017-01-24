package edu.rutgers.css.Rutgers.channels.reader.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSAdapter;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSItem;
import edu.rutgers.css.Rutgers.channels.reader.model.loader.RSSItemLoader;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.FuncUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * RSS feed reader
 */
public class RSSReader extends DtableChannelFragment implements LoaderManager.LoaderCallbacks<List<RSSItem>> {

    /* Log tag and component handle */
    private static final String TAG                 = "RSSReader";
    public static final String HANDLE               = "reader";
    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Constants */
    public static final int EXPIRE                  = 1;
    public static final TimeUnit EXPIRE_UNIT        = TimeUnit.MINUTES;

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_URL_TAG         = ComponentFactory.ARG_URL_TAG;

    /* Saved instance state tags */
    private static final String SAVED_DATA_TAG      = Config.PACKAGE_NAME + ".reader.data";

    /* Member data */
    private ArrayList<RSSItem> mData;
    private RSSAdapter mAdapter;

    public RSSReader() {
        // Required empty public constructor
    }

    /** Create arugment bundle for an RSS feed reader */
    public static Bundle createArgs(@NonNull String title, @NonNull String url) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RSSReader.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_URL_TAG, url);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();

        mData = new ArrayList<>();
        mAdapter = new RSSAdapter(this.getActivity(), R.layout.row_rss, mData);

        if (savedInstanceState != null && savedInstanceState.getSerializable(SAVED_DATA_TAG) != null) {
            LOGD(TAG, "Restoring mData");
            mAdapter.addAll((ArrayList<RSSItem>) savedInstanceState.getSerializable(SAVED_DATA_TAG));
            return;
        }

        if (args.getString(ARG_URL_TAG) == null) {
            LOGE(TAG, "URL argument not set");
            Toast.makeText(getActivity(), R.string.failed_no_url, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final String title = getArguments().getString(ARG_TITLE_TAG);

        if (title == null) {
            LOGE(TAG, "No title tag provided");
            return;
        }

        mAdapter.getPositionClicks()
            .map(RSSItem::getLink)
            .filter(FuncUtils::nonNull)
            .map(link -> WebDisplay.createArgs(title, link))
            .compose(bindToLifecycle())
            .subscribe(this::switchFragments, this::logError);

        getErrorClicks()
            .doOnNext(view -> getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, getArguments(), this))
            .doOnNext(view -> showProgressCircle())
            .compose(bindToLifecycle())
            .subscribe();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);

        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        }

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mData.isEmpty()) outState.putSerializable(SAVED_DATA_TAG, mData);
    }

    @Override
    public Loader<List<RSSItem>> onCreateLoader(int id, Bundle args) {
        return new RSSItemLoader(getContext(), args.getString(ARG_URL_TAG));
    }

    @Override
    public void onLoadFinished(Loader<List<RSSItem>> loader, List<RSSItem> data) {
        reset();

        if (data.isEmpty()) {
            handleErrorWithRetry(new Exception("Something went wrong when getting RSS"));
            return;
        }

        mData = new ArrayList<>(data);
        mAdapter.addAll(mData);
    }

    @Override
    public void onLoaderReset(Loader<List<RSSItem>> loader) {
        reset();
    }

    @Override
    protected void reset() {
        super.reset();
        mAdapter.clear();
    }
}
