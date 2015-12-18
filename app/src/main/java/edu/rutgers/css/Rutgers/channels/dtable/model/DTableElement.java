package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;

/**
 * Basic DTable element with title that may be localized by campus.
 */
public class DTableElement implements Serializable {

    private VarTitle varTitle;
    private DTableElement parent;

    public DTableElement(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        // Set the element title. JSON may have a string or object containing campus-local strings
        varTitle = new VarTitle(jsonObject.get("title"));
        this.parent = parent;
    }

    public DTableElement getParent() {
        return parent;
    }

    /**
     * Get element title.
     * @return Element title, default to home title if campus-localized.
     */
    public String getTitle() {
        return varTitle.getTitle();
    }

    /**
     * Get element title based on home campus.
     * @param homeCampus User's home campus
     * @return Campus-localized element title
     */
    public String getTitle(@NonNull String homeCampus) {
        return varTitle.getTitle(homeCampus);
    }

}
