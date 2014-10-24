package edu.rutgers.css.Rutgers.items;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Basic DTable element with title that may be localized by campus.
 */
public class DTableElement {

    static class VarTitle {
        public String homeCampus;
        public String homeTitle;
        public String foreignTitle;
    }

    private VarTitle varTitle;

    public DTableElement(JSONObject jsonObject) throws JSONException {
        // Set the element title. JSON may have a string or object containing campus-local strings
        varTitle = new VarTitle();
        Object titleObject = jsonObject.get("title");
        if(titleObject.getClass() == String.class) {
            varTitle.homeTitle = jsonObject.getString("title");
        } else if(titleObject.getClass() == JSONObject.class) {
            varTitle.homeCampus = ((JSONObject)titleObject).getString("homeCampus");
            varTitle.homeTitle = ((JSONObject)titleObject).getString("homeTitle");
            varTitle.foreignTitle = ((JSONObject)titleObject).getString("foreignTitle");
        }
    }

    /**
     * Get element title.
     * @return Element title, default to home title if campus-localized.
     */
    public String getTitle() {
        return varTitle.homeTitle;
    }

    /**
     * Get element title based on home campus.
     * @param homeCampus User's home campus
     * @return Campus-localized element title
     */
    public String getTitle(@NonNull String homeCampus) {
        if(varTitle.homeCampus == null) {
            return varTitle.homeTitle;
        } else if(homeCampus.equalsIgnoreCase(varTitle.homeCampus)) {
            return varTitle.homeTitle;
        } else {
            return varTitle.foreignTitle;
        }
    }

}
