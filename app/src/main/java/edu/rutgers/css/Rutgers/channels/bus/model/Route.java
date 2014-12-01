package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus route. Contains list of stops on the route.
 */
public final class Route {
    private String title;
    @SerializedName("stops") private List<String> stopTags;
    private String agencyTag; // Not part of Nextbus results

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getStopTags() {
        return stopTags;
    }

    public String getAgencyTag() {
        return agencyTag;
    }

    void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;
    }
}
