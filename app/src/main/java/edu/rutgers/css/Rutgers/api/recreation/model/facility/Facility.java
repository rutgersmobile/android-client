package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

/**
 * Recreation facility. Construct with GSON.
 */
@Data
public class Facility {

    private final String title;
    private final String address;
    @SerializedName("information_number") final private String informationNumber;
    @SerializedName("business_number") final private String businessNumber;
    @SerializedName("full_description") final private String fullDescription;
    @SerializedName("brief_description") final private String briefDescription;
    @SerializedName("daily_schedules") final private List<FacilityDaySchedule> dailySchedules;

    @Override
    public String toString() {
        return getTitle();
    }

}
