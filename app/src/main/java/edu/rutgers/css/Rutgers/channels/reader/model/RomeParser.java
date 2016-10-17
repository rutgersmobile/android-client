package edu.rutgers.css.Rutgers.channels.reader.model;

import com.rometools.modules.mediarss.MediaEntryModuleImpl;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.api.ParseException;
import edu.rutgers.css.Rutgers.api.XmlParser;

/**
 * Parser for RSS and Atom feeds powered by Rome
 */
public class RomeParser implements XmlParser<List<RSSItem>> {

    private final static DatePrinter printer = FastDateFormat.getInstance("MMM dd, yyyy, h:mm a", Locale.US);

    private Response response;
    public void setResponse(final Response response) {
        this.response = response;
    }

    @Override
    public List<RSSItem> parse(InputStream in) throws ParseException, IOException {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new InputSource(in));

            final List<RSSItem> items = new ArrayList<>();
            for (final SyndEntry entry : feed.getEntries()) {
                final Module module = entry.getModule(MediaModule.URI);
                final String title = entry.getTitle();
                final SyndContent description = entry.getDescription();
                final String link = entry.getLink();
                final String author = entry.getAuthor();
                final Date publishedDate = entry.getPublishedDate();
                final Date updatedDate = entry.getUpdatedDate();
                final List<SyndEnclosure> enclosures = entry.getEnclosures();
                String url = null;
                for (final SyndEnclosure enclosure : enclosures) {
                    if (enclosure.getType().contains("image")) {
                        url = enclosure.getUrl();
                    }
                }
                if (module instanceof MediaEntryModuleImpl) {
                    final MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) module;
                    for (final MediaContent content : mediaModule.getMediaContents()) {
                        if (content.getMedium().equals("image")) {
                            url = content.getReference().toString();
                        }
                    }
                }
                items.add(new RSSItem(
                        title,
                        description.getValue(),
                        link,
                        author,
                        publishedDate != null ? printer.format(publishedDate) : null,
                        updatedDate != null ? printer.format(updatedDate) : null,
                        updatedDate != null ? printer.format(updatedDate) : null,
                        url
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
