package edu.rutgers.css.Rutgers.channels.bus.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus route. Contains list of stops on the route.
 */
public class Route {
    private String title;
    @SerializedName("stops") private List<String> stopTags;

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getStopTags() {
        return stopTags;
    }
}
