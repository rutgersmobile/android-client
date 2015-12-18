package edu.rutgers.css.Rutgers.channels.dtable.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

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

    public DTableRoot(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        super(jsonObject, parent);

        JsonPrimitive p = jsonObject.getAsJsonPrimitive("schema");
        if (p != null) {
            schema = p.getAsString();
        }

        p = jsonObject.getAsJsonPrimitive("grouped");
        grouped = p != null && p.getAsBoolean();

        p = jsonObject.getAsJsonPrimitive("window");
        window = p != null && p.getAsBoolean();

        JsonArray childrenJson = jsonObject.getAsJsonArray("children");
        children = new ArrayList<>(childrenJson.size());

        for (JsonElement childElement : childrenJson) {
            JsonObject child = childElement.getAsJsonObject();
            if (child.has("answer")) children.add(new DTableFAQ(child, this));
            else if (child.has("children") && child.get("children").isJsonArray())
                children.add(new DTableRoot(child, this));
            else if (child.has("channel")) children.add(new DTableChannel(child, this));
            else if (child.has("title")) children.add(new DTableElement(child, this));
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
