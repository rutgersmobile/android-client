package edu.rutgers.css.Rutgers.channels.reader.model.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.channels.reader.fragments.RSSReader;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSItem;
import edu.rutgers.css.Rutgers.channels.reader.model.RSSXmlParser;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Loader for RSS feeds
 */
public class RSSItemLoader extends AsyncTaskLoader<List<RSSItem>> {
    private static final String TAG = "RSSItemLoader";

    private String url;
    private List<RSSItem> data;

    public RSSItemLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    public List<RSSItem> loadInBackground() {
        List<RSSItem> rssItems = new ArrayList<>();
        try {
            rssItems.addAll(ApiRequest.new_xml(url, RSSReader.EXPIRE, new RSSXmlParser()));
        } catch (XmlPullParserException | IOException e) {
            LOGE(TAG, e.getMessage());
            AppUtils.showFailedLoadToast(getContext());
        }
        return rssItems;
    }

    @Override
    public void deliverResult(List<RSSItem> holder) {
        if (isReset()) {
            return;
        }

        List<RSSItem> oldItems = data;
        data = holder;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        data = null;
    }
}
