package edu.rutgers.css.Rutgers.channels.places.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

/**
 * Represents a building from the places database. Equivalent to a "place" JSON object from the
 * places API. Use GSON to construct.
 * @author James Chambers
 */
@Data
public final class Place {

    private String id;
    private String title;
    private String description;
    private String cid;
    @SerializedName("building_id") private String buildingID;
    @SerializedName("building_number") private String buildingNumber;
    @SerializedName("building_code") private String buildingCode;
    @SerializedName("campus_code") private String campusCode;
    @SerializedName("campus_name") private String campusName;
    private List<String> offices;
    private Location location;

    @Data
    public static final class Location {

        private String name;
        private String street;
        private String additional;
        private String city;
        private String state;
        @SerializedName("state_abbr") private String stateAbbr;
        @SerializedName("postal_code") private String postalCode;
        private String country;
        private double latitude;
        private double longitude;

    }

    @Override
    public String toString() {
        return getTitle();
    }

}
