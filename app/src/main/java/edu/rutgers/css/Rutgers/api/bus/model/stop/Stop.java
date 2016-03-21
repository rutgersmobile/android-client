package edu.rutgers.css.Rutgers.api.bus.model.stop;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.NextbusItem;
import lombok.Data;

/**
 * Nextbus stop. Contains stop location and serviced routes.
 */
@Data
public final class Stop implements NextbusItem {
    private String title;
    @SerializedName("routes") private final List<String> routeTags;
    @SerializedName("lat") private final String latitude;
    @SerializedName("lon") private final String longitude;
    private final String stopId;
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
