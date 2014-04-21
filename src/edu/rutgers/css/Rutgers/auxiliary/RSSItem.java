package edu.rutgers.css.Rutgers.auxiliary;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.androidquery.util.XmlDom;

public class RSSItem {

	private static final String TAG = "RSSItem";
	
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
