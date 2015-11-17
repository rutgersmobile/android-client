package edu.rutgers.css.Rutgers.channels.dtable.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Specifies a channel to open and arguments to pass to it.
 */
public class DTableChannel extends DTableElement {

    private String view;
    private String url;
    private String data;
    private VarTitle channelTitle;
    private int count;

    public DTableChannel(JsonObject jsonObject) throws JsonSyntaxException {
        super(jsonObject);

        JsonObject channel = jsonObject.getAsJsonObject("channel");
        view = channel.getAsJsonPrimitive("view").getAsString();
        if (channel.has("title") && !channel.get("title").isJsonNull()) channelTitle = new VarTitle(channel.get("title"));
        if (channel.has("url") && !channel.get("url").isJsonNull()) url = channel.getAsJsonPrimitive("url").getAsString();
        if (channel.has("data") && !channel.get("data").isJsonNull()) data = channel.getAsJsonPrimitive("data").getAsString();
        if (channel.has("count")) {
            count = channel.getAsJsonPrimitive("count").getAsInt();
        } else {
            count = 0;
        }
    }

    /**
     * Get channel display title.
     * @return Channel display title, default to home title if campus-localized.
     */
    public String getChannelTitle() {
        if (channelTitle == null) return getTitle();
        else return channelTitle.getTitle();
    }

    /**
     * Get channel display title based on home campus.
     * @param homeCampus User's home campus
     * @return Campus-localized channel display title
     */
    public String getChannelTitle(String homeCampus) {
        if (channelTitle == null) return getTitle(homeCampus);
        else return channelTitle.getTitle(homeCampus);
    }

    public String getView() {
        return view;
    }

    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public int getCount() {
        return count;
    }

}
