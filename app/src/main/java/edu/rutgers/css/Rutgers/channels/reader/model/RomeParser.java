package edu.rutgers.css.Rutgers.channels.reader.model;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ParseException;
import edu.rutgers.css.Rutgers.api.XmlParser;

/**
 * Parser for RSS and Atom feeds powered by Rome
 */
public class RomeParser implements XmlParser<List<RSSItem>> {
    @Override
    public List<RSSItem> parse(InputStream in) throws ParseException, IOException {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new InputSource(in));

            final List<RSSItem> items = new ArrayList<>();
            for (final SyndEntry entry : feed.getEntries()) {
                final String title = entry.getTitle();
                final SyndContent description = entry.getDescription();
                final String link = entry.getLink();
                final String author = entry.getAuthor();
                final Date publishedDate = entry.getPublishedDate();
                final Date updatedDate = entry.getUpdatedDate();
                final String uri = entry.getUri();
                items.add(new RSSItem(
                        title,
                        description.getValue(),
                        link,
                        author,
                        publishedDate != null ? publishedDate.toString() : null,
                        updatedDate != null ? updatedDate.toString() : null,
                        updatedDate != null ? updatedDate.toString() : null,
                        uri
                ));
            }
            return items;
        } catch (FeedException e) {
            throw new ParseException(e);
        } finally {
            in.close();
        }
    }
}
