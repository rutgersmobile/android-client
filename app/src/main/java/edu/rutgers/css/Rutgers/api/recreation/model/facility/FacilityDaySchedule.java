package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Facility meeting area hours for a single day. Construct with GSON.
 */
@Data
public class FacilityDaySchedule implements Serializable {

    private final String date;
    @SerializedName("meeting_area_hours") final private List<MeetingAreaHours> areaHours;
}
