package edu.rutgers.css.Rutgers.auxiliary;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;

import android.util.Log;

import com.androidquery.util.XmlDom;

/**
 * Class for holding RSS item data
 *
 */
public class RSSItem {

	private static final String TAG = "RSSItem";
	
	private String title;
	private String description;
	private String link;
	private String author;
	private String date;
	private String imgUrl;
	
	/**
	 * Default constructor takes RSS item as XML object
	 * @param item RSS item in XML form
	 */
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
			DateFormat eventDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE", Locale.US);
			DateFormat outDf = new SimpleDateFormat("E, MMM dd, h:mm a", Locale.US);
			DateFormat outEndDf = new SimpleDateFormat("h:mm a", Locale.US);
			
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
	
	/**
	 * Get decoded RSS item title
	 * @return HTML escaped item title
	 */
	public String getTitle() {
		return StringEscapeUtils.unescapeHtml4(this.title);
	}
	
	/**
	 * Get decoded RSS item description
	 * @return HTML escaped item description
	 */
	public String getDescription() {
		return StringEscapeUtils.unescapeHtml4(this.description);
	}
	
	/**
	 * Get RSS item link
	 * @return RSS item link
	 */
	public String getLink() {
		return this.link;
	}
	
	/**
	 * Get decoded RSS item author
	 * @return HTML escaped item author
	 */
	public String getAuthor() {
		return StringEscapeUtils.unescapeHtml4(this.author);
	}
	
	/**
	 * Get RSS item date
	 * @return Item date (as string)
	 */
	public String getDate() {
		return this.date;
	}
	
	/**
	 * Get item image URL
	 * @return Item image URL
	 */
	public String getImgUrl() {
		return this.imgUrl;
	}
	
	/**
	 * toString returns RSS item title
	 * @return RSS item title
	 */
	public String toString() {
		return StringEscapeUtils.unescapeHtml4(this.title);
	}
	
}
