package edu.rutgers.css.Rutgers.channels.dtable.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableAdapter;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableChannel;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableElement;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * Dynamic Table
 * <p>Use {@link #dTag()} instead of TAG when logging</p>
 */
public class DTable extends Fragment {
    
    private static final String TAG = "DTable";
    public static final String HANDLE = "dtable";

    private static final String TAG_HANDLE = "mHandle";
    private static final String TAG_ROOT = "mDRoot";

    private DTableRoot mDRoot;
    private List<DTableElement> mData;
    private DTableAdapter mAdapter;
    private String mURL;
    private String mAPI;
    private String mHandle;

    public DTable() {
        // Required empty public constructor
    }

    /**
     * Get descriptive DTable tag to use for logging.
     * @return DTable tag combined with current handle, if possible
     */
    private String dTag() {
        if(mHandle != null) return TAG + "_" + mHandle;
        else return TAG;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.getString("mData") != null) {
            Log.v(dTag(), "Restoring mData");
            mHandle = savedInstanceState.getString(TAG_HANDLE);
            mDRoot = (DTableRoot) savedInstanceState.getSerializable(TAG_ROOT);
            mAdapter = new DTableAdapter(getActivity(), mData);
            return;
        }

        // Didn't restore adapter & data from state; initialize here
        mData = new ArrayList<DTableElement>();
        mAdapter = new DTableAdapter(getActivity(), mData);

        Bundle args = getArguments();

        // Get handle for this DTable instance
        if(args.getString("handle") != null) mHandle = args.getString("handle");
        else if(args.getString("api") != null) mHandle = args.getString("api").replace(".txt","");
        else if(args.getString("title") != null) mHandle = args.getString("title");
        else mHandle = "null";

        // If a JSON array was provided in "data" field, load it
        if (args.getSerializable("data") != null) {
            try {
                mDRoot = (DTableRoot) args.getSerializable("data");
                mData = mDRoot.getChildren();
                mAdapter.setData(mData);
                return;
            } catch (ClassCastException e) {
                Log.e(dTag(), "onCreateView(): " + e.getMessage());
            }
        }

        // Otherwise, check for URL or API argument
        else if (args.getString("url") != null) mURL = args.getString("url");
        else if (args.getString("api") != null) mAPI = args.getString("api");
        else {
            Log.e(dTag(), "DTable must have URL, API, or data in its arguments bundle");
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
            return;
        }

        Promise<JSONObject, AjaxStatus, Double> promise;
        if(mURL != null) promise = Request.json(mURL, Request.CACHE_ONE_HOUR);
        else promise = Request.api(mAPI, Request.CACHE_ONE_HOUR);

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(promise).done(new DoneCallback<JSONObject>() {

            @Override
            public void onDone(JSONObject result) {
                try {
                    mDRoot = new DTableRoot(result);
                    Log.v(dTag(), "Loaded DTable root: " + mDRoot.getTitle());
                    mData = mDRoot.getChildren();
                    mAdapter.setData(mData);
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

        });
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dtable, parent, false);
        ListView listView = (ListView) v.findViewById(R.id.dtable_list);

        final Bundle args = getArguments();
        if(args.getString("title") != null) {
            getActivity().setTitle(args.getString("title"));
        }
        
        listView.setAdapter(mAdapter);

        final String homeCampus = RutgersUtils.getHomeCampus(getActivity());
        
        // Clicks on DTable item launch component in "view" field with arguments
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DTableElement element = (DTableElement) parent.getAdapter().getItem(position);
                Bundle newArgs = new Bundle();

                // This is another DTable root
                if (mAdapter.getItemViewType(position) == DTableAdapter.ViewTypes.CAT_TYPE.ordinal()) {
                    newArgs.putString("component", "dtable");
                    newArgs.putString("title", element.getTitle(homeCampus));
                    newArgs.putSerializable("data", element);
                }
                // This is a FAQ button
                else if (mAdapter.getItemViewType(position) == DTableAdapter.ViewTypes.FAQ_TYPE.ordinal()) {
                    // Toggle pop-down visibility
                    mAdapter.togglePopdown(position);
                    return;
                }
                // This object has a channel
                else {
                    DTableChannel channel = (DTableChannel) element;
                    // Must have view and title set to launch a channel
                    newArgs.putString("component", channel.getView());
                    newArgs.putString("title", channel.getChannelTitle(homeCampus));

                    // Add the rest of the fields to the arg bundle
                    if(channel.getUrl() != null) newArgs.putString("url", channel.getUrl());
                    if(channel.getData() != null) newArgs.putString("data", channel.getData());
                    if(channel.getCount() > 0) newArgs.putInt("count", channel.getCount());
                }

                ComponentFactory.getInstance().switchFragments(newArgs);
            }

        });
        
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If any data was actually loaded, save it in state
        if(mData != null && mData.size() > 0) {
            outState.putSerializable(TAG_ROOT, mDRoot);
            outState.putString(TAG_HANDLE, mHandle);
        }
    }

}