package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import edu.rutgers.css.Rutgers2.R;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.auxiliary.RSSAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RSSItem;

/**
 * RSS Feed display fragment
 * Displays items from an RSS feed.
 */
public class RSSReader extends Fragment implements OnItemClickListener {	
	
	private static final String TAG = "RSSReader";
	private List<RSSItem> rssItems;
	private ListView mList;
	private ArrayAdapter<RSSItem> rssItemAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		
		rssItems = new ArrayList<RSSItem>();
		rssItemAdapter = new RSSAdapter(this.getActivity(), R.layout.rss_row, rssItems);

		if(args.get("url") == null) {
			Log.e(TAG, "null rss url");
			return;
		}
		
		// Get RSS feed XML and add items through the array adapter
		Request.xml(args.getString("url")).done(new DoneCallback<XmlDom>() {
			
			@Override
			public void onDone(XmlDom xml) {
				List<XmlDom> items = xml.tags("item");
				
				for(XmlDom item: items) {
					RSSItem newItem = new RSSItem(item);
					rssItemAdapter.add(newItem);
				}
			}
			
		}).fail(new FailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, e.getError() + "; " + e.getMessage() + "; Response code: " + e.getCode());
			}
			
		});
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_rssreader, parent, false);
		Bundle args = getArguments();
		mList = (ListView) v.findViewById(R.id.rssreader_list);
		
		Log.d(TAG, "Fragment for RSS feed " + args.getString("rss"));
		
		// Sets title to name of the RSS feed being displayed
		getActivity().setTitle(args.getString("title"));
		
		// Adapter & click listener for RSS list view
		mList.setAdapter(rssItemAdapter);
		mList.setOnItemClickListener(this);		
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// For now send URL to browser
		RSSItem item = rssItemAdapter.getItem(position);
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(item.link));
		startActivity(i);
	}
	
}
