package edu.rutgers.css.Rutgers.api.bus.model.stop;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.NextbusItem;
import lombok.Data;

/**
 * Nextbus stop tags grouped by stop title.
 */
@Data
public final class StopGroup implements NextbusItem {
    private String title;
    @SerializedName("tags") private final List<String> stopTags;
    private final String geoHash;
    private String agencyTag; // Not part of Nextbus results

    @Override
    public String getTag() {
        return getTitle();
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
