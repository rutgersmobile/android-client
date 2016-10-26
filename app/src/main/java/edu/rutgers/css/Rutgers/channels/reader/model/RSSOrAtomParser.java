package edu.rutgers.css.Rutgers.channels.reader.model;

import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ParseException;
import edu.rutgers.css.Rutgers.api.XmlParser;
import lombok.Setter;

/**
 * Parser that chooses another parser to use
 */
public class RSSOrAtomParser implements XmlParser<List<RSSItem>> {
    private static final String atomType = "application/atom+xml";

    @Setter
    private Response response;

    @Override
    public List<RSSItem> parse(InputStream in) throws ParseException, IOException {
        final XmlParser<List<RSSItem>> parser = !response.request().httpUrl().host().contains("ruevents.rutgers.edu")
                ? new RomeParser()
                : new RSSXmlParser();
        return parser.parse(in);
    }
}
