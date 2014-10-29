package edu.rutgers.css.Rutgers.items;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Handles titles localized by campus.
 */
public class VarTitle implements Serializable {

    public String homeCampus;
    public String homeTitle;
    public String foreignTitle;

    public VarTitle(Object titleObject) throws JSONException {
        if(titleObject.getClass() == String.class) {
            homeTitle = (String) titleObject;
        } else if(titleObject.getClass() == JSONObject.class) {
            homeCampus = ((JSONObject)titleObject).getString("homeCampus");
            homeTitle = ((JSONObject)titleObject).getString("homeTitle");
            foreignTitle = ((JSONObject)titleObject).getString("foreignTitle");
        } else {
            throw new IllegalArgumentException("Title must be a String or JSONObject");
        }
    }

    /**
     * Get element title.
     * @return Element title, default to home title if campus-localized.
     */
    public String getTitle() {
        return homeTitle;
    }

    /**
     * Get element title based on home campus.
     * @param homeCampus User's home campus
     * @return Campus-localized element title
     */
    public String getTitle(@NonNull String homeCampus) {
        if(this.homeCampus == null) {
            return homeTitle;
        } else if(this.homeCampus.equalsIgnoreCase(homeCampus)) {
            return homeTitle;
        } else {
            return foreignTitle;
        }
    }

}
