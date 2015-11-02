package edu.rutgers.css.Rutgers.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.JsonUtils;

/**
 * RU Mobile channel.
 */
public class Channel implements Serializable {

    private VarTitle title;
    private String handle;
    private String view;
    private String api;
    private String url;
    private JsonArray data;
    private boolean canOverride;

    /** Construct channel from JSON. */
    public Channel(JsonObject channelJson) throws JsonSyntaxException {
        this.title = new VarTitle(channelJson.get("title"));
        this.handle = channelJson.getAsJsonPrimitive("handle").getAsString();

        if (channelJson.get("view").isJsonNull() && !channelJson.get("url").isJsonNull()) {
            this.view = WebDisplay.HANDLE;
        } else {
            this.view = channelJson.getAsJsonPrimitive("view").getAsString();
        }

        if (JsonUtils.exists(channelJson, "api")) this.api = channelJson.getAsJsonPrimitive("api").getAsString();
        if (JsonUtils.exists(channelJson, "url")) this.url = channelJson.getAsJsonPrimitive("url").getAsString();
        if (JsonUtils.exists(channelJson, "data")) this.data = channelJson.getAsJsonArray("data");

        JsonElement elem = channelJson.get("canOverride");

        this.canOverride = elem != null && !elem.isJsonNull() && elem.getAsBoolean();
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

    public JsonArray getData() {
        return data;
    }

    public Bundle getBundle(){

        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, getView());
        bundle.putString(ComponentFactory.ARG_HANDLE_TAG, getHandle());
        bundle.putString(ComponentFactory.ARG_TITLE_TAG, getTitle());
        if (StringUtils.isNotBlank(getApi())) {
            bundle.putString(ComponentFactory.ARG_API_TAG, getApi());
        }
        if (StringUtils.isNotBlank(getUrl())) {
            bundle.putString(ComponentFactory.ARG_URL_TAG, getUrl());
        }
        if (getData() != null) {
            bundle.putString(ComponentFactory.ARG_DATA_TAG, getData().toString());
        }
        return bundle;
    }

}
