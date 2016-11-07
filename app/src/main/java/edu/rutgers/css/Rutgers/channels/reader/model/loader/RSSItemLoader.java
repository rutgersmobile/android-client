package edu.rutgers.css.Rutgers.channels.reader.model.loader;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.oldapi.ApiRequest;
import edu.rutgers.css.Rutgers.oldapi.ParseException;
import edu.rutgers.css.Rutgers.channels.reader.fragments.RSSReader;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSItem;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSOrAtomParser;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Loader for RSS feeds
 */
public class RSSItemLoader extends SimpleAsyncLoader<List<RSSItem>> {
    private static final String TAG = "RSSItemLoader";

    private String url;

    public RSSItemLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    public List<RSSItem> loadInBackground() {
        List<RSSItem> rssItems = new ArrayList<>();
        try {
            rssItems.addAll(ApiRequest.xml(url, RSSReader.EXPIRE, RSSReader.EXPIRE_UNIT, new RSSOrAtomParser()));
        } catch (ParseException | IOException e) {
            LOGE(TAG, e.getMessage());
        }
        return rssItems;
    }
}
