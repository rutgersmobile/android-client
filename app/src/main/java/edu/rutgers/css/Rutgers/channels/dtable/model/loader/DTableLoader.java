package edu.rutgers.css.Rutgers.channels.dtable.model.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Generic DTable loader
 */
public class DTableLoader extends AsyncTaskLoader<DTableRoot> {
    private String url;
    private String api;
    private String tag;

    private DTableRoot data;

    public DTableLoader(Context context, String url, String api, String tag) {
        super(context);
        this.url = url;
        this.api = api;
        this.tag = tag + "Loader";
    }

    @Override
    public DTableRoot loadInBackground() {
        DTableRoot root = null;
        try {
            JsonObject json;
            if (url != null) {
                json = ApiRequest.json(url, ApiRequest.CACHE_ONE_HOUR, JsonObject.class);
            } else {
                json = ApiRequest.api(api, ApiRequest.CACHE_ONE_HOUR, JsonObject.class);
            }

            root = new DTableRoot(json);
            LOGV(tag, "Loaded DTable root: " + root.getTitle());
        } catch (JsonSyntaxException | IOException e) {
            LOGW(tag, e.getMessage());
        }

        return root;
    }

    @Override
    public void deliverResult(DTableRoot holder) {
        if (isReset()) {
            return;
        }

        DTableRoot oldItems = data;
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
