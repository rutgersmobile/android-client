package edu.rutgers.css.Rutgers.channels.dtable.model.loader;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Generic DTable loader
 */
public class DTableLoader extends SimpleAsyncLoader<DTableRoot> {
    private String url;
    private String api;
    private String tag;

    /**
     * Only one of either url or api is required. Url is used if both are supplied
     * @param context Application context
     * @param url Location of resource that contains data to build DTableRoot
     * @param api Resource name on mobile server
     * @param tag Logging tag from calling class
     */
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
                json = ApiRequest.json(url, TimeUnit.HOURS, JsonObject.class);
            } else {
                json = ApiRequest.api(api, TimeUnit.HOURS, JsonObject.class);
            }

            root = new DTableRoot(json, null);
            LOGV(tag, "Loaded DTable root: " + root.getTitle());
        } catch (JsonSyntaxException | IOException e) {
            LOGW(tag, e.getMessage());
        }

        return root;
    }
}
