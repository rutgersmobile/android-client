package edu.rutgers.css.Rutgers.channels.reader.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSAdapter;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSItem;
import edu.rutgers.css.Rutgers.channels.reader.model.loader.RSSItemLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * RSS feed reader
 */
public class RSSReader extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<List<RSSItem>> {

    /* Log tag and component handle */
    private static final String TAG                 = "RSSReader";
    public static final String HANDLE               = "reader";
    private static final int LOADER_ID              = 1;

    /* Constants */
    public static final int EXPIRE                = 1;
    public static final TimeUnit EXPIRE_UNIT       = TimeUnit.MINUTES;

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_URL_TAG         = ComponentFactory.ARG_URL_TAG;

    /* Saved instance state tags */
    private static final String SAVED_DATA_TAG      = Config.PACKAGE_NAME + ".reader.data";

    /* Member data */
    private ArrayList<RSSItem> mData;
    private RSSAdapter mAdapter;
    private boolean mLoading;
    private ShareActionProvider shareActionProvider;

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

        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_progress);

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
        if (args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        }

        // Adapter & click listener for RSS list view
        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RSSItem item = mAdapter.getItem(position);

                if (item.getLink() != null) {
                    // Open web display fragment
                    switchFragments(WebDisplay.createArgs(args.getString(ARG_TITLE_TAG), item.getLink()));
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
            setShareIntent();
        }
    }

    @Override
    public ShareActionProvider getShareActionProvider() {
        return shareActionProvider;
    }

    @Override
    public Link getLink() {
        final Bundle args = getArguments();
        final String topHandle = args.getString(ComponentFactory.ARG_HANDLE_TAG);
        final List<String> history = args.getStringArrayList(ComponentFactory.ARG_HIST_TAG);
        final String pathPart = args.getString(ARG_TITLE_TAG);

        if (topHandle != null && history != null && pathPart != null) {
            final List<String> linkArgs = new ArrayList<>();
            for (final String title : history) {
                linkArgs.add(title);
            }
            linkArgs.add(pathPart.replaceAll("\\s+", "").toLowerCase());
            return new Link(topHandle, linkArgs, getLinkTitle());
        }

        return null;
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
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
        mAdapter.clear();
        mData = new ArrayList<>(data);
        mAdapter.addAll(mData);
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public void onLoaderReset(Loader<List<RSSItem>> loader) {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}
