package edu.rutgers.css.Rutgers.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.R.id;
import edu.rutgers.css.Rutgers.R.layout;
import edu.rutgers.css.Rutgers.api.Request;

public class RSSReader extends Fragment implements OnItemClickListener {
	
	private class RSSItem {
		public String title;
		public String description;
		public String link;
		public String author;
		public String date;
		public String imgUrl;
		
		public RSSItem(XmlDom item) {
			if(item == null) return;
			
			// RSS 2.0 required fields
			this.title = item.text("title");
			this.description = item.text("description");
			this.link = item.text("link");
			
			// Non-required fields
			this.author = item.text("author");
			
			// Get date - check pubDate for news or event:xxxDateTime for events
			if(item.text("pubDate") != null) {
				this.date = item.text("pubDate");
			}
			else if(item.text("event:beginDateTime") != null) {
				// Event time - parse start & end timestamps and produce an output string that gives
				// the date and beginning and end times, e.g. "Fri, Apr 18, 10:00 AM - 11:00 AM"
				DateFormat eventDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE");
				DateFormat outDf = new SimpleDateFormat("E, MMM dd, h:mm a");
				DateFormat outEndDf = new SimpleDateFormat("h:mm a");
				
				try {
					Date parsedDate = eventDf.parse(item.text("event:beginDateTime"));
					Date parsedEnd = eventDf.parse(item.text("event:endDateTime"));
					this.date = outDf.format(parsedDate) + " - " + outEndDf.format(parsedEnd);
				} catch (ParseException e) {
					Log.e(TAG, "Failed to parse event date \"" + item.text("event:beginDateTime")+"\"");
					this.date = item.text("event:beginDateTime");
				}
			}
			
			// Image may be in url field (enclosure url attribute in the rutgers feed)
			if(item.child("enclosure") != null) {
				this.imgUrl = item.child("enclosure").attr("url");
			}
			else {
				this.imgUrl = item.text("url");
			}
		}
		
		public String toString() {
			return this.title;
		}
		
	}
	
	public class RSSAdapter extends ArrayAdapter<RSSItem> {

		private int layoutResource;
		
		/* Class to hold data for RSS list rows */
		private final class RSSHolder {
				ImageView iconImageView;
				TextView titleTextView;
				TextView authordateTextView;
				TextView descriptionTextView;
		}
		
		public RSSAdapter(Context context, int resource, List<RSSItem> objects) {
			super(context, resource, objects);
			this.layoutResource = resource;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			RSSHolder mHolder = null;
			
			/* Make new data holder or get existing one */
			if(convertView == null) {
					mHolder = new RSSHolder();
					/* Create new RSS Row view */
					convertView = mLayoutInflater.inflate(this.layoutResource, null);
					convertView.setTag(mHolder);
			}
			else {	
					mHolder = (RSSHolder)convertView.getTag();
			}

			/* Populate RSS row layout elements */
			mHolder.iconImageView = (ImageView) convertView.findViewById(R.id.rssRowIconView);
			mHolder.titleTextView = (TextView) convertView.findViewById(R.id.rssRowTitleView);
			mHolder.authordateTextView = (TextView) convertView.findViewById(R.id.rssRowAuthorDateView);
			mHolder.descriptionTextView = (TextView) convertView.findViewById(R.id.rssRowDescView);
			
			mHolder.titleTextView.setText(this.getItem(position).title);
			mHolder.authordateTextView.setText(this.getItem(position).date);
			mHolder.descriptionTextView.setText(this.getItem(position).description);
			
			return convertView;
		}
		
	}
	
	private static final String TAG = "RSSReader";
	private List<RSSItem> rssItems;
	private ArrayAdapter<RSSItem> rssItemAdapter;
	private AQuery aq;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rssItems = new ArrayList<RSSItem>();
		rssItemAdapter = new RSSAdapter(this.getActivity(), R.layout.rss_row, rssItems);

		aq = new AQuery(this.getActivity());
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_rssreader, parent, false);
		Bundle args = getArguments();
		final ListView mList = (ListView) v.findViewById(R.id.rssreader_list);
		
		Log.d(TAG, "Fragment for RSS feed " + args.getString("rss"));
		
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// For now send URL to browser
		RSSItem item = rssItemAdapter.getItem(position);
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(item.link));
		startActivity(i);
	}

	private void setupList(View v, String rssUrl) {
		final ListView mList = (ListView) v.findViewById(R.id.rssreader_list);
		
		/*
		RSSItem dummy = new RSSItem(null);
		dummy.title = "Article Title";
		dummy.author = "Author Name";
		dummy.url = "http://news.google.com/";
		dummy.description = "This is a fake article entry. This is a fake article entry. This is a fake article entry. This is a fake article entry. This is a fake article entry.";
		dummy.date = "April 17th, 2014 - 11:00 AM";
		rssItemAdapter.add(dummy);
		*/
		
		// Get RSS feed XML and add items through the array adapter
		Request.xml(rssUrl).done(new DoneCallback<XmlDom>() {
			
			@Override
			public void onDone(XmlDom xml) {
				List<XmlDom> items = xml.tags("item");
				
				for(XmlDom item: items) {
					RSSItem newItem = new RSSItem(item);
					rssItemAdapter.add(newItem);
					//Log.d(TAG,"Adding RSS item " + newItem);
				}
			}
			
		}).fail(new FailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, e.getError() + "; " + e.getMessage() + "; Response code: " + e.getCode());
			}
			
		});
		
		// Redraw list
		mList.invalidate();
	}
	
}
