package edu.rutgers.css.Rutgers.channels.dtable.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableChannel;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableElement;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Dynamic Table
 * <p>Use {@link #dTag()} instead of TAG when logging</p>
 */
public class DTable extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "DTable";
    public static final String HANDLE = "dtable";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_HANDLE_TAG      = ComponentFactory.ARG_HANDLE_TAG;
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
    private boolean mLoading;

    /* View references */
    private ProgressBar mProgressCircle;

    public DTable() {
        // Required empty public constructor
    }

    /** Creates basic argument bundle - fields common to all bundles */
    private static Bundle baseArgs(@NonNull String title, @NonNull String handle) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, DTable.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_HANDLE_TAG, handle);
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from a URL. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, @NonNull URL url) {
        Bundle bundle = baseArgs(title, handle);
        bundle.putString(ARG_URL_TAG, url.toString());
        return bundle;
    }

    /** Create argument bundle for a DTable that loads from the RUMobile API. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, @NonNull String api) {
        Bundle bundle = baseArgs(title, handle);
        bundle.putString(ARG_API_TAG, api);
        return bundle;
    }

    /** Create argument bundle for a DTable that launches with pre-loaded table data. */
    public static Bundle createArgs(@NonNull String title, @NonNull String handle, @NonNull DTableRoot tableRoot) {
        Bundle bundle = baseArgs(title, handle);
        bundle.putSerializable(ARG_DATA_TAG, tableRoot);
        return bundle;
    }

    /**
     * Get channel-specific DTable tag to use for logging.
     * @return DTable tag combined with current handle, if possible
     */
    private String dTag() {
        if(mHandle != null) return TAG + "_" + mHandle;
        else return TAG;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();

        List<DTableElement> data = new ArrayList<>();
        mAdapter = new DTableAdapter(getActivity(), data);

        // If recreating, restore state
        if(savedInstanceState != null && savedInstanceState.getSerializable(SAVED_ROOT_TAG) != null) {
            mHandle = savedInstanceState.getString(SAVED_HANDLE_TAG);
            mDRoot = (DTableRoot) savedInstanceState.getSerializable(SAVED_ROOT_TAG);
            mAdapter.addAll(mDRoot.getChildren());
            Log.d(dTag(), "Restoring mData");
            return;
        }

        // Get handle for this DTable instance
        if(args.getString(ARG_HANDLE_TAG) != null) mHandle = args.getString(ARG_HANDLE_TAG);
        else if(args.getString(ARG_API_TAG) != null) mHandle = args.getString(ARG_API_TAG).replace(".txt","");
        else if(args.getString(ARG_TITLE_TAG) != null) mHandle = args.getString(ARG_TITLE_TAG);
        else mHandle = "invalid";

        // If table data was provided in "data" field, load it
        if (args.getSerializable(ARG_DATA_TAG) != null) {
            try {
                mDRoot = (DTableRoot) args.getSerializable(ARG_DATA_TAG);
                mAdapter.addAll(mDRoot.getChildren());
                return;
            } catch (ClassCastException e) {
                Log.e(dTag(), "onCreateView(): " + e.getMessage());
            }
        }

        // Otherwise, check for URL or API to load table from
        else if (args.getString(ARG_URL_TAG) != null) mURL = args.getString(ARG_URL_TAG);
        else if (args.getString(ARG_API_TAG) != null) mAPI = args.getString(ARG_API_TAG);
        else {
            Log.e(dTag(), "DTable must have URL, API, or data in its arguments bundle");
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            return;
        }

        Promise<JSONObject, AjaxStatus, Double> promise =
                (mURL != null) ? Request.json(mURL, Request.CACHE_ONE_HOUR) :
                                 Request.api(mAPI, Request.CACHE_ONE_HOUR);

        mLoading = true;

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(promise).done(new DoneCallback<JSONObject>() {

            @Override
            public void onDone(JSONObject result) {
                try {
                    mDRoot = new DTableRoot(result);
                    Log.v(dTag(), "Loaded DTable root: " + mDRoot.getTitle());
                    mAdapter.addAll(mDRoot.getChildren());
                } catch (JSONException e) {
                    Log.e(dTag(), "onCreate(): " + e.getMessage());
                    AppUtils.showFailedLoadToast(getActivity());
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                Log.w(dTag(), AppUtils.formatAjaxStatus(status));
                AppUtils.showFailedLoadToast(getActivity());
            }

        }).always(new AlwaysCallback<JSONObject, AjaxStatus>() {
            @Override
            public void onAlways(Promise.State state, JSONObject resolved, AjaxStatus rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_dtable, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if(mLoading) showProgressCircle();

        final Bundle args = getArguments();
        if(args.getString(ARG_TITLE_TAG) != null) {
            getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        }

        final ListView listView = (ListView) v.findViewById(R.id.dtable_list);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mAdapter);
        alphaInAnimationAdapter.setAbsListView(listView);
        assert alphaInAnimationAdapter.getViewAnimator() != null;
        alphaInAnimationAdapter.getViewAnimator().setInitialDelayMillis(500);
        listView.setAdapter(mAdapter);

        final String homeCampus = RutgersUtils.getHomeCampus(getActivity());

        mAdapter.setExpandCollapseListener(new ExpandableListItemAdapter.ExpandCollapseListener() {
            @Override
            public void onItemExpanded(int position) {
                DTableElement element = mAdapter.getItem(position);

                // FAQ View expansion is handled by adapter. If it's not a FAQ row, don't expand it.
                if (mAdapter.getItemViewType(position) == DTableAdapter.ViewTypes.FAQ_TYPE.ordinal()) {
                    return;
                } else {
                    mAdapter.collapse(position);
                }

                // DTable root - launch a new DTable
                if (mAdapter.getItemViewType(position) == DTableAdapter.ViewTypes.CAT_TYPE.ordinal()) {
                    String newHandle = mHandle + "_" + element.getTitle(homeCampus).replace(" ", "_").toLowerCase();
                    Bundle newArgs = DTable.createArgs(element.getTitle(homeCampus), newHandle, (DTableRoot) element);
                    ComponentFactory.getInstance().switchFragments(newArgs);
                }
                // Channel row - launch channel
                else {
                    DTableChannel channel = (DTableChannel) element;
                    Bundle newArgs = new Bundle();
                    // Must have view and title set to launch a channel
                    newArgs.putString(ComponentFactory.ARG_COMPONENT_TAG, channel.getView());
                    newArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getChannelTitle(homeCampus));

                    // Add optional fields to the arg bundle
                    if (channel.getUrl() != null) newArgs.putString(ComponentFactory.ARG_URL_TAG, channel.getUrl());
                    if (channel.getData() != null) newArgs.putString(ComponentFactory.ARG_DATA_TAG, channel.getData());
                    if (channel.getCount() > 0) newArgs.putInt(ComponentFactory.ARG_COUNT_TAG, channel.getCount());
                    ComponentFactory.getInstance().switchFragments(newArgs);
                }
            }

            @Override
            public void onItemCollapsed(int position) {

            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If any data was actually loaded, save it in state
        if(mDRoot != null) {
            outState.putSerializable(SAVED_ROOT_TAG, mDRoot);
            outState.putString(SAVED_HANDLE_TAG, mHandle);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}