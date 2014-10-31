package edu.rutgers.css.Rutgers.items.Recreation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Recreation facility. Construct with GSON.
 */
public class Facility {

    private String title;
    private String address;
    @SerializedName("information_number") private String informationNumber;
    @SerializedName("business_number") private String businessNumber;
    @SerializedName("full_description") private String fullDescription;
    @SerializedName("brief_description") private String briefDescription;
    @SerializedName("area_hours") private List<AreaHours> areaHours;

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

    public List<AreaHours> getAreaHours() {
        return areaHours;
    }

}
