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
    private int count;

    public DTableChannel(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        JSONObject channel = jsonObject.getJSONObject("channel");

        view = channel.getString("view");
        if(!channel.isNull("url")) url = channel.getString("url");
        if(!channel.isNull("data")) data = channel.getString("data");
        count = channel.optInt("count");
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
