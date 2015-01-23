package edu.rutgers.css.Rutgers.channels.places.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents a building from the places database. Equivalent to a "place" JSON object from the
 * places API. Use GSON to construct.
 * @author James Chambers
 */
public class Place {

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

    public static class Location {

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

        public String getName() {
            return name;
        }

        public String getStreet() {
            return street;
        }

        public String getAdditional() {
            return additional;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getStateAbbr() {
            return stateAbbr;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCountry() {
            return country;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCid() {
        return cid;
    }

    public String getBuildingID() {
        return buildingID;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public String getCampusName() {
        return campusName;
    }

    public List<String> getOffices() {
        return offices;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
