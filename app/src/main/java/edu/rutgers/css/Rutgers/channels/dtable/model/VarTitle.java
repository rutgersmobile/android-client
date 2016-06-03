package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;

/**
 * Handles titles localized by campus.
 */
public class VarTitle implements Serializable {

    public String homeCampus;
    public String homeTitle;
    public String foreignTitle;

    public VarTitle(VarTitle other) {
        this.homeCampus = other.homeCampus;
        this.homeTitle = other.homeTitle;
        this.foreignTitle = other.foreignTitle;
    }

    public VarTitle(String homeTitle) {
        this.homeTitle = homeTitle;
    }

    public VarTitle(String homeTitle, String homeCampus, String foreignTitle) {
        this.homeTitle = homeTitle;
        this.homeCampus = homeCampus;
        this.foreignTitle = foreignTitle;
    }

    public VarTitle(JsonElement element) throws JsonSyntaxException {
        try {
            homeTitle = element.getAsString();
        } catch (ClassCastException | UnsupportedOperationException e) {
            try {
                JsonObject json = element.getAsJsonObject();

                homeCampus = json.getAsJsonPrimitive("homeCampus").getAsString();
                homeTitle = json.getAsJsonPrimitive("homeTitle").getAsString();
                foreignTitle = json.getAsJsonPrimitive("foreignTitle").getAsString();
            } catch (ClassCastException e2) {
                throw new IllegalArgumentException("Title must be a String or JSONObject");
            }
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
        if (this.homeCampus == null) {
            return homeTitle;
        } else if (this.homeCampus.equalsIgnoreCase(homeCampus)) {
            return homeTitle;
        } else {
            return foreignTitle;
        }
    }

}
