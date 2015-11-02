package edu.rutgers.css.Rutgers.channels.reader.model;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/** Represents an item in a news or events feed */
public class RSSItem implements Serializable {

    private static final String TAG = "RSSItem";

    /** Date format: "Mon, 26 May 2014 -0400" */
    private final static DateParser rssDf = FastDateFormat.getInstance("EEE, dd MMM yyyy ZZZZZ", Locale.US);

    /** Date format: "Monday, May 26, 2014" */
    private final static DateParser rssDf2 = FastDateFormat.getInstance("EEE, MMM dd, yyyy", Locale.US);

    /** Date format: "Mon, 26 May 2014 00:27:50 GMT" */
    private final static DateParser rssDf3 = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /** Date format: "May 26, 2014" */
    private final static DatePrinter rssOutFormat = FastDateFormat.getInstance("MMMM d, yyyy", Locale.US);

    /** Date format: "2014-05-26 00:27:50 GMT" */
    private final static DateParser eventDf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss EEE", Locale.US);

    /** Date format: "Apr 12, 2014 9:30 AM" */
    private final static DatePrinter eventOutDf = FastDateFormat.getInstance("MMM dd, yyyy, h:mm a", Locale.US);

    /** Date format: "Apr 12, 2014" */
    private final static DatePrinter eventOutDayOnlyDf = FastDateFormat.getInstance("MMM dd, yyyy", Locale.US);

    /** Date format: "9:30 AM" */
    private final static DatePrinter eventOutTimeOnlyDf = FastDateFormat.getInstance("h:mm a", Locale.US);

    private final static Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));

    private String title;
    private String description;
    private String link;
    private String author;
    private String date;
    private URL imgUrl;

    public RSSItem(String title, String description, String link, String author, String pubDate, String beginDateTime, String endDateTime, String url) {
        this.title = sanitizeString(title);
        this.description = sanitizeStringWithTags(description);
        this.link = link;
        this.author = sanitizeString(author);

        // Get date - check pubDate for news or event:xxxDateTime for events
        if (pubDate != null) {
            /* Try to parse the pubDate using the standard pubDate format, as well as some
             * variations seen in the feeds we read */
            DateParser[] parsers = {rssDf, rssDf2, rssDf3};
            Date parsed = null;

            for (DateParser parser : parsers) {
                try {
                    parsed = parser.parse(pubDate);
                } catch (ParseException ignored) {
                }

                if (parsed != null) {
                    this.date = rssOutFormat.format(parsed);
                    break;
                }
            }

            if (parsed == null) {
                // Couldn't parse the date, just display it as is
                this.date = sanitizeString(pubDate);
            } else if (parsed.getTime() == 0) {
                // A shameful timestamp.
                this.date = null;
            }
        } else if (beginDateTime != null) {
            // Event time - parse start & end timestamps and produce an output string that gives
            // the date and beginning and end times, e.g. "Fri, Apr 18, 10:00 AM - 11:00 AM"
            // Events feed dates are in Eastern time.
            try {
                Date eventBegin = eventDf.parse(beginDateTime);

                if (eventBegin.getTime() == 0) {
                    // Swag. Fun. Nice.
                    this.date = null;
                } else {
                    // Not all feeds supply endDateTime ¯\_(ツ)_/¯
                    if (endDateTime != null) {
                        Date eventEnd = eventDf.parse(endDateTime);
                        this.date = formatEventDateRange(eventBegin, eventEnd);
                    } else {
                        this.date = formatEventDate(eventBegin);
                    }
                }
            } catch (ParseException e) {
                LOGW(TAG, "Failed to parse event date \"" + beginDateTime + "\"", e);
                this.date = beginDateTime;
            }
        }

        try {
            this.imgUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGE(TAG, "Bad image URL: " + e.getMessage());
            this.imgUrl = null;
        }
    }

    /** Trim whitespace and newlines, and decode HTML symbols */
    private String sanitizeString(String string) {
        return StringUtils.trim(StringUtils.chomp(StringEscapeUtils.unescapeHtml4(string)));
    }

    /** Trim whitespace and newlines, remove HTML tags, and decode HTML symbols */
    private String sanitizeStringWithTags(String string) {
        return StringUtils.trim(StringUtils.chomp(AppUtils.stripTags(StringEscapeUtils.unescapeHtml4(string))));
    }

    /*
     * The timestamps 00:00:00 and 23:59:50 are interpreted to mean that there
     * is no specific start/end time for the event - they last all day.
     */

    /** Get the string representation of an event date. */
    private String formatEventDate(Date date) {
        if (isMidnight(date) || isEndOfDay(date)) {
            return eventOutDayOnlyDf.format(date);
        } else {
            return eventOutDf.format(date);
        }
    }

    /** Get the string representation of an event date range. */
    private String formatEventDateRange(Date beginDate, Date endDate) {
        if (DateUtils.isSameDay(beginDate, endDate)) {
            if (isEndOfDay(endDate)) {
                // Ending at 23:59:50 means it lasts all day, just show the date
                return formatEventDate(beginDate);
            } else if (isMidnight(beginDate)) {
                // The event starts at midnight but has an end time before the end of day (weird case)
                return formatEventDate(beginDate) + " until " + eventOutDayOnlyDf.format(endDate);
            } else {
                // The event has normal begin and end times (hopefully)
                return formatEventDate(beginDate) + " - " + eventOutTimeOnlyDf.format(endDate);
            }
        } else {
            return formatEventDate(beginDate) + " - " + formatEventDate(endDate);
        }
    }

    /** Check if the time is set to 00:00:00 */
    private boolean isMidnight(Date date) {
        cal.setTime(date);
        return (cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0);
    }

    /** Check if the time is set to 23:59:50 */
    private boolean isEndOfDay(Date date) {
        cal.setTime(date);
        return (cal.get(Calendar.HOUR_OF_DAY) == 23) && (cal.get(Calendar.MINUTE) == 59) && (cal.get(Calendar.SECOND) == 50);
    }

    /** Get decoded RSS item title */
    public String getTitle() {
        return this.title;
    }
    
    /** Get decoded RSS item description */
    public String getDescription() {
        return this.description;
    }
    
    /** Get RSS item link */
    public String getLink() {
        return this.link;
    }
    
    /** Get decoded RSS item author */
    public String getAuthor() {
        return this.author;
    }
    
    /** Get RSS item date */
    public String getDate() {
        return this.date;
    }
    
    /** Get item image URL */
    public URL getImgUrl() {
        return this.imgUrl;
    }
    
    /** Returns RSS item title */
    @Override
    public String toString() {
        return this.title;
    }

}
