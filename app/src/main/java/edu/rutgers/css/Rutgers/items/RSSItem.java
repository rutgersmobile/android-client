package edu.rutgers.css.Rutgers.items;

import android.util.Log;

import com.androidquery.util.XmlDom;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class for holding RSS item data
 *
 */
public class RSSItem implements Serializable {

	private static final String TAG = "RSSItem";
	
	private String title;
	private String description;
	private String link;
	private String author;
	private String date;
	private URL imgUrl;

	private final static DateFormat rssDf = new SimpleDateFormat("EEE, dd MMM yyyy ZZZZZ", Locale.US); // Mon, 26 May 2014 -0400
	private final static DateFormat rssDf2 = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US); // Monday, May 26, 2014
    private final static DateFormat rssDf3 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US); // Mon, 26 May 2014 00:27:50 GMT
    private final static DateFormat rssOutFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US); // May 26, 2014
    private final static DateFormat eventDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE", Locale.US);
    private final static DateFormat eventOutDf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US);
    private final static DateFormat eventOutEndDf = new SimpleDateFormat("h:mm a", Locale.US);
	
	/**
	 * Default constructor takes RSS item as XML object
	 * @param item RSS item in XML form
	 */
	public RSSItem(XmlDom item) {
		if(item == null) return;

		// RSS 2.0 required fields
		this.title = StringUtils.chomp(StringEscapeUtils.unescapeHtml4(item.text("title")));
		this.description = StringUtils.chomp(stripTags(StringEscapeUtils.unescapeHtml4(item.text("description"))));
        if(this.description != null) this.description = this.description.trim();
		this.link = item.text("link");
		
		// Author is currently unused as its presence in feeds is too sporadic
		this.author = StringUtils.chomp(StringEscapeUtils.unescapeHtml4(item.text("author")));
		
		// Get date - check pubDate for news or event:xxxDateTime for events
		if(item.text("pubDate") != null) {
			// Try to parse the pubDate
			Date parsed = null;
			
			// Try the standard pubDate format, as well as some variations seen in the feeds we read
			try {
				parsed = rssDf.parse(item.text("pubDate"));
			} catch(ParseException e) {}
			 
			if(parsed == null) {
				try {
					parsed = rssDf2.parse(item.text("pubDate"));
				}
				catch(ParseException e) {}
			}
			
			if(parsed == null) {
				try {
					parsed = rssDf3.parse(item.text("pubDate"));
				} catch(ParseException e) {}
			}
			
			if(parsed != null) {
				this.date = rssOutFormat.format(parsed);
			}
			else {
				// Couldn't parse the date, just display it as is
				this.date = item.text("pubDate");
			}
		}
		else if(item.text("event:beginDateTime") != null) {
			// Event time - parse start & end timestamps and produce an output string that gives
			// the date and beginning and end times, e.g. "Fri, Apr 18, 10:00 AM - 11:00 AM"
            // Events feed dates are in Eastern time.
			try {
				Date eventBegin = eventDf.parse(item.text("event:beginDateTime"));

                // Not all feeds supply endDateTime ¯\_(ツ)_/¯
                if(item.text("event:endDateTime") != null) {
                    Date eventEnd = eventDf.parse(item.text("event:endDateTime"));

                    // If days match show day with start & end hours
                    if(isSameDay(eventBegin, eventEnd)) this.date = eventOutDf.format(eventBegin) + " - " + eventOutEndDf.format(eventEnd);
                    // Otherwise show start and end dates
                    else this.date = eventOutDf.format(eventBegin) + " - " + eventOutDf.format(eventEnd);
                }
                else {
                    this.date = eventOutDf.format(eventBegin);
                }

			} catch (ParseException e) {
				Log.w(TAG, "Failed to parse event date \"" + item.text("event:beginDateTime")+"\"");
				this.date = item.text("event:beginDateTime");
			}
		}
		
		// Image may be in url field (enclosure url attribute in the Rutgers feed)
		try {
			if(item.child("enclosure") != null) this.imgUrl = new URL(item.child("enclosure").attr("url"));
            else if(item.child("media:thumbnail") != null) this.imgUrl = new URL(item.child("media:thumbnail").attr("url"));
			else if(item.child("url") != null) this.imgUrl = new URL(item.text("url"));
			else this.imgUrl = null;
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
			this.imgUrl = null;
		}
	}
	
	/**
	 * Get decoded RSS item title
	 * @return HTML escaped item title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Get decoded RSS item description
	 * @return HTML escaped item description
	 */
	public String getDescription() {
		return this.description;
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
		return this.author;
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
	public URL getImgUrl() {
		return this.imgUrl;
	}
	
	/**
	 * toString returns RSS item title
	 * @return RSS item title
	 */
    @Override
	public String toString() {
		return this.title;
	}

    /**
     * Check if two different timestamps have the same date.
     * @param d1 Date 1
     * @param d2 Date 2
     * @return True if days of year match, false if not
     */
    private boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance(Locale.US);
        Calendar cal2 = Calendar.getInstance(Locale.US);
        cal1.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal2.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Remove HTML tags from string
     * @param string String to cleanse
     * @return Cleansed string, or null if string was null
     */
    private String stripTags(String string) {
        if(string == null) return null;
        return string.replaceAll("\\<.*?\\>", "");
    }

}
