package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Facility meeting area hours for a single day. Construct with GSON.
 */
public class FacilityDaySchedule implements Serializable {
    private final String date;
    @SerializedName("meeting_area_hours") final private List<MeetingAreaHours> areaHours;

    public FacilityDaySchedule(final String date, final List<MeetingAreaHours> areaHours) {
        this.date = date;
        this.areaHours = areaHours;
    }

    public String getDate() {
        return date;
    }

    public List<MeetingAreaHours> getAreaHours() {
        return areaHours;
    }
}
