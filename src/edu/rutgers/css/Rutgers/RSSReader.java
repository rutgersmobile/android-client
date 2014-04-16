package edu.rutgers.css.Rutgers;

import java.util.ArrayList;
import java.util.List;

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
		
		public RSSItem(String title, String description, String url) {
			this.title = title;
			this.description = description;
			this.url = url;
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
		final ListView mList = (ListView) v.findViewById(R.id.rssreader_list);
		Bundle args = getArguments();
		
		getActivity().setTitle(args.getString("title"));
		
		// TODO Request RSS data & parse it for list
		Log.d("RSSReader", "Fragment for RSS feed " + args.getString("rss"));
			
		mList.setAdapter(rssItemAdapter);
		mList.setOnItemClickListener(this);
		
		aq.ajax(args.getString("rss"), XmlDom.class, this, "rssCallback");
		
		return v;
	}
	
	private void setupList() {
		
	}

	public void rssCallback(String url, XmlDom xml, AjaxStatus status) {
		List<XmlDom> items = xml.tags("item");
		
		for(XmlDom item: items) {
			Log.d("RSSReader", item.text("title") + "," + item.text("description") + item.text("link"));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
}
