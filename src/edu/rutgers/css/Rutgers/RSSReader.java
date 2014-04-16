package edu.rutgers.css.Rutgers;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

public class RSSReader extends Fragment implements OnItemClickListener {

	private class RSSItem {
		public String title;
		public String description;
		public String url;
		public String author;
		public String date;
		
		public RSSItem(XmlDom item) {
			this.title = item.text("title");
			this.description = item.text("description");
			this.url = item.text("url");
			this.author = item.text("author");
		}
		
		public String toString() {
			return this.title;
		}
		
	}
	
	private List<RSSItem> rssItems;
	private ArrayAdapter<RSSItem> rssItemAdapter;
	private AQuery aq;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		aq = new AQuery(this.getActivity());
		rssItems = new ArrayList<RSSItem>();
		rssItemAdapter = new ArrayAdapter<RSSItem>(this.getActivity(), android.R.layout.simple_list_item_1, rssItems);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_rssreader, parent, false);
		Bundle args = getArguments();
		final ListView mList = (ListView) v.findViewById(R.id.rssreader_list);
		
		Log.d("RSSReader", "Fragment for RSS feed " + args.getString("rss"));
		
		// Sets title to name of the RSS feed being displayed
		getActivity().setTitle(args.getString("title"));
		
		// Adapter & click listener for RSS list view
		mList.setAdapter(rssItemAdapter);
		mList.setOnItemClickListener(this);
		
		// Populate the list with given RSS feed
		setupList(v, args.getString("rss"));
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	private void setupList(View v, String rssUrl) {
		final ListView mList = (ListView) v.findViewById(R.id.rssreader_list);
		
		// Get RSS feed XML and add items through the array adapter
		Request.xml(rssUrl).done(new DoneCallback<XmlDom>() {
			
			@Override
			public void onDone(XmlDom xml) {
				List<XmlDom> items = xml.tags("item");
				
				for(XmlDom item: items) {
					RSSItem newItem = new RSSItem(item);
					rssItemAdapter.add(newItem);
					Log.d("RSSReader","Adding RSS item " + newItem);
				}
			}
			
		}).fail(new FailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e("RSSReader", e.getError() + "; " + e.getMessage() + "; Response code: " + e.getCode());
			}
			
		});
		
		// Redraw list
		mList.invalidate();
	}
	
}
