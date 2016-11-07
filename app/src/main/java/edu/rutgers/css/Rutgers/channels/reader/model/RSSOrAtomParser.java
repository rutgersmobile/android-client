package edu.rutgers.css.Rutgers.channels.reader.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.rutgers.css.Rutgers.oldapi.ParseException;
import edu.rutgers.css.Rutgers.oldapi.XmlParser;
import okhttp3.Response;

/**
 * Parser that chooses another parser to use
 */
public class RSSOrAtomParser implements XmlParser<List<RSSItem>> {
    private static final String atomType = "application/atom+xml";

    private Response response;

    @Override
    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public List<RSSItem> parse(InputStream in) throws ParseException, IOException {
        final XmlParser<List<RSSItem>> parser = !response.request().url().host().contains("ruevents.rutgers.edu")
                ? new RomeParser()
                : new RSSXmlParser();
        return parser.parse(in);
    }
}
