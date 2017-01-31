package edu.rutgers.css.Rutgers.channels.dtable.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableChannel;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableGridAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableLinearAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.oldapi.ApiRequest;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.GridSpacingItemDecoration;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Dynamic Table
 * <p>Use {@link #dTag()} instead of TAG when logging</p>
 */
public class DTable extends DtableChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "DTable";
    public static final String HANDLE               = "dtable";

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

    public static final int GRID_SPACING = 25;

    /* Member data */
    private DTableRoot mDRoot;
    private DTableAdapter mAdapter;
    private String mURL;
    private String mApi;
    private String mHandle;
    private String mTopHandle;
    private String mTitle;
    private String mLayout;
    private List<DTableRoot.BannerItem> banner;

    private CarouselView carouselView;

    public DTable() {
        // Required empty public constructor
    }

    /** Creates basic argument bundle - fields common to all bundles */
    private static Bundle baseArgs(@NonNull String title, @NonNull String handle, @NonNull String topHandle, @NonNull String layout) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, DTable.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_HANDLE_TAG, handle);
        bundle.putString(ARG_TOP_HANDLE_TAG, topHandle);
        bundle.putString(ComponentFactory.ARG_LAYOUT_TAG, layout);
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from a URL. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull String layout, @NonNull URL url) {
        Bundle bundle = baseArgs(title, handle, topHandle, layout);
        bundle.putString(ARG_URL_TAG, url.toString());
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from the RUMobile API. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull String layout, @NonNull String api) {
        Bundle bundle = baseArgs(title, handle, topHandle, layout);
        bundle.putString(ARG_API_TAG, api);
        return bundle;
    }

    /** Create argument bundle for a DTable that launches with pre-loaded table data. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, String topHandle, @NonNull String layout, @NonNull DTableRoot tableRoot) {
        Bundle bundle = baseArgs(title, handle, topHandle, layout);
        bundle.putSerializable(ARG_DATA_TAG, tableRoot);
        return bundle;
    }

    public static Bundle createChannelArgs(DTableChannel channel, String homeCampus, String topHandle, ArrayList<String> history) {
        final Bundle bundle = new Bundle();
        // Must have view and title set to launch a channel
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, channel.getView());
        bundle.putString(ComponentFactory.ARG_TITLE_TAG, channel.getChannelTitle(homeCampus));
        bundle.putString(ComponentFactory.ARG_TOP_HANDLE_TAG, topHandle);
        bundle.putStringArrayList(ComponentFactory.ARG_HIST_TAG, history);

        // Add optional fields to the arg bundle
        if (channel.getUrl() != null)
            bundle.putString(ComponentFactory.ARG_URL_TAG, channel.getUrl());
        if (channel.getData() != null)
            bundle.putString(ComponentFactory.ARG_DATA_TAG, channel.getData());
        if (channel.getCount() > 0)
            bundle.putInt(ComponentFactory.ARG_COUNT_TAG, channel.getCount());

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

        banner = new ArrayList<>();

        final Bundle args = getArguments();

        // Get handle for this DTable instance
        final String handle = args.getString(ARG_HANDLE_TAG);
        mApi = args.getString(ARG_API_TAG);
        mTitle = args.getString(ARG_TITLE_TAG);
        if (handle != null) {
            mHandle = handle;
        } else if (mApi != null) {
            mHandle = mApi.replace(".txt","");
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
                mAdapter = createAdapter();
                LOGD(dTag(), "Restoring mData");
                return;
            }
        }

        try {
            final DTableRoot dataRoot = (DTableRoot) args.getSerializable(ARG_DATA_TAG);
            // If table data was provided in "data" field, load it
            if (dataRoot != null) {
                mDRoot = dataRoot;
                mAdapter = createAdapter();
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
            } else if (mApi == null) {
                LOGE(dTag(), "DTable must have URL, API, or data in its arguments bundle");
                Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mLayout = args.getString(ComponentFactory.ARG_LAYOUT_TAG, "linear");
        if (mLayout.equals("linear")) {
            mAdapter = new DTableLinearAdapter(
                getContext(),
                new ArrayList<>(),
                getFragmentMediator(),
                mHandle,
                mTopHandle,
                RutgersUtils.getHomeCampus(getContext()),
                new ArrayList<>()
            );
        } else {
            mAdapter = new DTableGridAdapter(
                getContext(),
                new ArrayList<>(),
                getFragmentMediator(),
                mHandle,
                mTopHandle,
                RutgersUtils.getHomeCampus(getContext()),
                new ArrayList<>()
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDRoot != null) {
            return;
        }

        // Start loading DTableRoot object in another thread
        setLoading(true);
        final String url = mURL;
        Observable.fromCallable(() -> {
            JsonObject json;
            if (url != null) {
                json = ApiRequest.json(url, TimeUnit.HOURS, JsonObject.class);
            } else {
                json = ApiRequest.api(mApi, TimeUnit.HOURS, JsonObject.class);
            }
            return new DTableRoot(json, null);
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .retryWhen(this::logAndRetry)
        .subscribe(root -> {
            reset();
            mDRoot = root;
            banner.addAll(root.getBanner());
            if (carouselView != null) {
                carouselView.setPageCount(banner.size());
                if (banner.size() != 0) {
                    carouselView.setVisibility(View.VISIBLE);
                }
                carouselView.invalidate();
            }
            mAdapter.addAll(root.getChildren(RutgersUtils.getHomeCampus(getContext())));
            mAdapter.addAllHistory(root.getHistory());
        }, this::handleErrorWithRetry);
    }

    private DTableAdapter createAdapter() {
        banner.addAll(mDRoot.getBanner());
        if (mDRoot.getLayout().equals("linear")) {
            mLayout = "linear";
            return createLinearAdapter();
        } else {
            mLayout = "grid";
            return createGridAdapter();
        }
    }

    private DTableLinearAdapter createLinearAdapter() {
        return new DTableLinearAdapter(
            getContext(),
            mDRoot.getChildren(),
            getFragmentMediator(),
            mHandle,
            mTopHandle,
            RutgersUtils.getHomeCampus(getContext()),
            mDRoot.getHistory()
        );
    }

    private DTableGridAdapter createGridAdapter() {
        return new DTableGridAdapter(
            getContext(),
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
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_carousel_recycler_progress);

        if (mTitle != null) {
            getActivity().setTitle(mTitle);
        }

        carouselView = (CarouselView) v.findViewById(R.id.carousel);
        if (banner.size() != 0) {
            carouselView.setVisibility(View.VISIBLE);
        }
        carouselView.setPageCount(banner.size());
        carouselView.setImageListener((position, imageView) -> {
            final DTableRoot.BannerItem bannerItem = banner.get(position);
            Picasso.with(getContext())
                .load(Config.API_BASE + "img/" + bannerItem.getImage())
                .into(imageView);
            imageView.setOnClickListener(view ->
                getFragmentMediator().deepLink(bannerItem.getUrl(), true)
            );
        });

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        // makes fling work for some reason
        recyclerView.setNestedScrollingEnabled(false);

        if (mLayout.equals("linear")) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, GRID_SPACING, true));
        }
        recyclerView.setItemAnimator(new FadeInAnimator());
        recyclerView.setAdapter((RecyclerView.Adapter) mAdapter);

        return v;
    }

    @Override
    public Link getLink() {
        final List<String> linkArgs = new ArrayList<>();
        linkArgs.addAll(mDRoot.getHistory());

        return new Link(mTopHandle, linkArgs, getLinkTitle(null));
    }

    @Override
    public VarTitle getLinkTitle(String homeCampus) {
        Channel channel = ((MainActivity)getActivity()).getChannelManager().getChannelByTag(mTopHandle);
        if (channel != null) {
            return channel.getVarTitle();
        } else if (mDRoot != null) {
            return mDRoot.getVarTitle();
        } else {
            return new VarTitle((String) getActivity().getTitle());
        }
    }

    @Override
    public String getChannelHandle() {
        return null;
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
    protected void reset() {
        super.reset();
        mAdapter.clear();
        mAdapter.clearHistory();
    }
}