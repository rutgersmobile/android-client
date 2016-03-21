package edu.rutgers.css.Rutgers.api.places.model;

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

    private final String id;
    private final String title;
    private final String description;
    private final String cid;
    @SerializedName("building_id") final private String buildingID;
    @SerializedName("building_number") final private String buildingNumber;
    @SerializedName("building_code") final private String buildingCode;
    @SerializedName("campus_code") final private String campusCode;
    @SerializedName("campus_name") final private String campusName;
    private final List<String> offices;
    private final Location location;

    @Data
    public static final class Location {

        private final String name;
        private final String street;
        private final String additional;
        private final String city;
        private final String state;
        @SerializedName("state_abbr") final private String stateAbbr;
        @SerializedName("postal_code") final private String postalCode;
        private final String country;
        private final double latitude;
        private final double longitude;

    }

    @Override
    public String toString() {
        return getTitle();
    }

}
