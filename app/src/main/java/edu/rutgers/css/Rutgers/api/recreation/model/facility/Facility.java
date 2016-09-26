package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Recreation facility. Construct with GSON.
 */
public class Facility {
    private final String title;
    private final String address;
    @SerializedName("information_number") final private String informationNumber;
    @SerializedName("business_number") final private String businessNumber;
    @SerializedName("full_description") final private String fullDescription;
    @SerializedName("brief_description") final private String briefDescription;
    @SerializedName("daily_schedules") final private List<FacilityDaySchedule> dailySchedules;

    public Facility(final String title, final String address, final String informationNumber,
                    final String businessNumber, final String fullDescription,
                    final String briefDescription, final List<FacilityDaySchedule> dailySchedules) {
        this.title = title;
        this.address = address;
        this.informationNumber = informationNumber;
        this.businessNumber = businessNumber;
        this.fullDescription = fullDescription;
        this.briefDescription = briefDescription;
        this.dailySchedules = dailySchedules;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getInformationNumber() {
        return informationNumber;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public List<FacilityDaySchedule> getDailySchedules() {
        return dailySchedules;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
