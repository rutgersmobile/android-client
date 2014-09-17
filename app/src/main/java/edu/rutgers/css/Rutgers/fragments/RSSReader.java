package edu.rutgers.css.Rutgers.fragments;

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
import com.androidquery.util.XmlDom;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.RSSAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.items.RSSItem;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * RSS Feed display fragment
 * Displays items from an RSS feed.
 */
public class RSSReader extends Fragment implements OnItemClickListener {	
	
	private static final String TAG = "RSSReader";
    public static final String HANDLE = "reader";

	private ArrayList<RSSItem> mData;
	private RSSAdapter mAdapter;
	private long expire = 60 * 1000; // cache feed for one minute
	
	public RSSReader() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		
		mData = new ArrayList<RSSItem>();
		mAdapter = new RSSAdapter(this.getActivity(), R.layout.row_rss, mData);

        if(savedInstanceState != null && savedInstanceState.getSerializable("mData") != null) {
            Log.v(TAG, "Restoring mData");
            mData.addAll((ArrayList<RSSItem>) savedInstanceState.getSerializable("mData"));
            mAdapter.notifyDataSetChanged();
            return;
        }

		if(args.getString("url") == null) {
			Log.e(TAG, "RSS feed URL was not set");
            Toast.makeText(getActivity(), R.string.failed_no_url, Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Get RSS feed XML and add items through the array adapter\
        AndroidDeferredManager dm = new AndroidDeferredManager();
		dm.when(Request.xml(args.getString("url"), expire)).done(new DoneCallback<XmlDom>() {
			
			@Override
			public void onDone(XmlDom xml) {
				List<XmlDom> items = xml.tags("item");
				
				for(XmlDom item: items) {
					RSSItem newItem = new RSSItem(item);
					mAdapter.add(newItem);
				}
			}

		}).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus e) {
                Log.e(TAG, AppUtil.formatAjaxStatus(e));
                AppUtil.showFailedLoadToast(getActivity());
            }

        });
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_rss_reader, parent, false);
		ListView listView = (ListView) v.findViewById(R.id.rssreader_list);
        Bundle args = getArguments();
		
		Log.v(TAG, "RSS Reader launched for " + args.getString("url"));
		
		// Sets title to name of the RSS feed being displayed
        if(args.getString("title") != null) {
            getActivity().setTitle(args.getString("title"));
        }

		// Adapter & click listener for RSS list view
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		
		return v;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!mData.isEmpty()) outState.putSerializable("mData", mData);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		RSSItem item = mAdapter.getItem(position);

        if(item.getLink() != null) {
            // Open web display fragment
            Bundle args = new Bundle();
            args.putString("component", WebDisplay.HANDLE);
            args.putString("url", item.getLink());

            ComponentFactory.getInstance().switchFragments(args);
        }
	}
	
}
