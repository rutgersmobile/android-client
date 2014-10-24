package edu.rutgers.css.Rutgers.items;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Specifies a channel to open and arguments to pass to it.
 */
public class DTableChannel extends DTableElement {

    private String view;
    private String url;
    private String data;
    private VarTitle channelTitle;
    private int count;

    public DTableChannel(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        JSONObject channel = jsonObject.getJSONObject("channel");
        view = channel.getString("view");
        if(!channel.isNull("title")) channelTitle = new VarTitle(channel.get("title"));
        if(!channel.isNull("url")) url = channel.getString("url");
        if(!channel.isNull("data")) data = channel.getString("data");
        count = channel.optInt("count");
    }

    /**
     * Get channel display title.
     * @return Channel display title, default to home title if campus-localized.
     */
    public String getChannelTitle() {
        if(channelTitle == null) return getTitle();
        else return channelTitle.getTitle();
    }

    /**
     * Get channel display title based on home campus.
     * @param homeCampus User's home campus
     * @return Campus-localized channel display title
     */
    public String getChannelTitle(String homeCampus) {
        if(channelTitle == null) return getTitle(homeCampus);
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
