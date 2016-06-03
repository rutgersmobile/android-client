package edu.rutgers.css.Rutgers.model;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.JsonUtils;
import lombok.Data;

/**
 * RU Mobile channel.
 */
@Data
public class Channel implements Serializable {

    private final VarTitle title;
    private final String handle;
    private final String view;
    private final String api;
    private final String url;
    private Link link;
    private final boolean canOverride;

    public Channel(Channel other) {
        this.title = new VarTitle(other.title);
        this.handle = other.handle;
        this.view = other.view;
        this.api = other.api;
        this.url = other.url;
        this.link = new Link(other.link);
        this.canOverride = other.canOverride;
    }

    /** Construct channel from JSON. */
    public Channel(JsonObject channelJson) throws JsonSyntaxException {
        this.title = new VarTitle(channelJson.get("title"));
        this.handle = channelJson.getAsJsonPrimitive("handle").getAsString();

        if (channelJson.get("view").isJsonNull() && !channelJson.get("url").isJsonNull()) {
            this.view = WebDisplay.HANDLE;
        } else {
            this.view = channelJson.getAsJsonPrimitive("view").getAsString();
        }

        if (JsonUtils.exists(channelJson, "api")) {
            this.api = channelJson.getAsJsonPrimitive("api").getAsString();
        } else {
            this.api = null;
        }

        if (JsonUtils.exists(channelJson, "url")) {
            this.url = channelJson.getAsJsonPrimitive("url").getAsString();
        } else {
            this.url = null;
        }

        if (JsonUtils.exists(channelJson, "link")) {
            this.link = Link.createLink(Uri.parse(channelJson.getAsJsonPrimitive("link").getAsString()), getTitle(), false);
        } else {
            this.link = Link.createLink(Uri.parse("rutgers://" + handle), getTitle(), false);
        }

        JsonElement elem = channelJson.get("canOverride");

        this.canOverride = elem != null && !elem.isJsonNull() && elem.getAsBoolean();
    }

    public String getTitle() {
        return title.getTitle();
    }

    public String getTitle(@NonNull String homeCampus) {
        return title.getTitle(homeCampus);
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
        if (getLink() != null) {
            bundle.putString(ComponentFactory.ARG_DATA_TAG, getLink().toString());
        }
        return bundle;
    }

}
