package edu.rutgers.css.Rutgers.channels.reader.model;

import android.util.Xml;

import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ParseException;
import edu.rutgers.css.Rutgers.api.XmlParser;

/**
 * Parser for getting RSS item information.
 */
public class RSSXmlParser implements XmlParser<List<RSSItem>> {
    private static final String ns = null;

    private Response response;

    @Override
    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public List<RSSItem> parse(InputStream in) throws ParseException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readRSS(parser);
        } catch (XmlPullParserException e) {
            throw new ParseException(e);
        } finally {
            in.close();
        }
    }

    private List<RSSItem> readRSS(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<RSSItem> items = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("channel")) {
                items.addAll(readChannel(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private List<RSSItem> readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<RSSItem> items = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {
                RSSItem item = readItem(parser);
                if (item != null) {
                    items.add(item);
                }
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private RSSItem readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = null;
        String description = null;
        String link = null;
        String author = null;
        String pubDate = null;
        String beginDateTime = null;
        String endDateTime = null;
        String url = null;
        parser.require(XmlPullParser.START_TAG, ns, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    title = readString(parser);
                    break;
                case "description":
                    description = readString(parser);
                    break;
                case "link":
                    link = readString(parser);
                    break;
                case "author":
                    author = readString(parser);
                    break;
                case "pubDate":
                    pubDate = readString(parser);
                    break;
                case "event:beginDateTime":
                    beginDateTime = readString(parser);
                    break;
                case "event:endDateTime":
                    endDateTime = readString(parser);
                    break;
                case "enclosure":
                    String urlAttr = parser.getAttributeValue(ns, "url");
                    if (!urlAttr.isEmpty()) {
                        url = urlAttr;
                    } else {
                        XmlPullParser urlParser = Xml.newPullParser();
                        urlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        urlParser.setInput(new StringReader(readString(parser)));
                        urlParser.nextTag();
                        url = readString(urlParser);
                    }
                    while (parser.next() != XmlPullParser.END_TAG) { }
                    break;
                case "media:thumbnail":
                    url = parser.getAttributeValue(ns, "url");
                    break;
                case "url":
                    url = readString(parser);
                    break;
                default:
                    skip(parser);
            }
        }
        return new RSSItem(title, description, link, author, pubDate, beginDateTime, endDateTime, url);
    }

    private String readString(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Expected start tag but got event type: " + parser.getEventType());
        }
        String text = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.TEXT) {
                text = parser.getText();
            }
        }
        return text;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
