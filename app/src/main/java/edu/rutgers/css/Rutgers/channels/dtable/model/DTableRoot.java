package edu.rutgers.css.Rutgers.channels.dtable.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Root DTable element: can contain a mix of channel, FAQ, and other DTable items.
 */
public class DTableRoot extends DTableElement {

    private String schema;
    private boolean grouped;
    private boolean window;
    private List<DTableElement> children;

    public DTableRoot(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        schema = jsonObject.optString("schema");
        grouped = jsonObject.optBoolean("grouped");
        window = jsonObject.optBoolean("window");

        JSONArray childrenJson = jsonObject.getJSONArray("children");
        children = new ArrayList<DTableElement>(childrenJson.length());

        for(int i = 0; i < childrenJson.length(); i++) {
            JSONObject child = childrenJson.getJSONObject(i);
            if(child.has("answer")) children.add(new DTableFAQ(child));
            else if(child.has("children") && child.opt("children") instanceof JSONArray) children.add(new DTableRoot(child));
            else if(child.has("channel")) children.add(new DTableChannel(child));
            else if(child.has("title")) children.add(new DTableElement(child));
        }
    }

    public String getSchema() {
        return schema;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public boolean isWindow() {
        return window;
    }

    public List<DTableElement> getChildren() {
        return children;
    }

}
