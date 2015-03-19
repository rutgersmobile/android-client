package edu.rutgers.css.Rutgers.model;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;

/**
 * RU Mobile channel.
 */
public class Channel {

    private String category;
    private VarTitle title;
    private String handle;
    private String view;
    private String api;
    private String url;
    private JSONArray data;
    private boolean canOverride;

    /** Construct channel from JSON. */
    public Channel(JSONObject channelJson) throws JSONException {
        this.category = channelJson.getString("category");
        this.title = new VarTitle(channelJson.get("title"));
        this.handle = channelJson.getString("handle");

        if (channelJson.isNull("view") && !channelJson.isNull("url")) {
            this.view = WebDisplay.HANDLE;
        } else {
            this.view = channelJson.getString("view");
        }

        if (!channelJson.isNull("api")) this.api = channelJson.getString("api");
        if (!channelJson.isNull("url")) this.url = channelJson.getString("url");
        if (!channelJson.isNull("data")) this.data = channelJson.getJSONArray("data");

        this.canOverride = channelJson.optBoolean("canOverride");
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title.getTitle();
    }

    public String getTitle(@NonNull String homeCampus) {
        return title.getTitle(homeCampus);
    }

    public String getHandle() {
        return handle;
    }

    public String getView() {
        return view;
    }

    public String getApi() {
        return api;
    }

    public String getUrl() {
        return url;
    }

    /**
     * This channel can supersede others with the same handle.
     */
    public boolean canOverride() {
        return canOverride;
    }

    public JSONArray getData() {
        return data;
    }

}
