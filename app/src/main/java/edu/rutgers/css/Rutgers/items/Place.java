package edu.rutgers.css.Rutgers.items;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jamchamb on 9/30/14.
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
            if(name != null && !name.isEmpty()) return name;
            else return null;
        }

        public String getStreet() {
            if(street != null && !street.isEmpty()) return street;
            else return null;
        }

        public String getAdditional() {
            if(additional != null && !additional.isEmpty()) return additional;
            else return null;
        }

        public String getCity() {
            if(city != null && !city.isEmpty()) return city;
            else return null;
        }

        public String getState() {
            if(state != null && !state.isEmpty()) return state;
            else return null;
        }

        public String getStateAbbr() {
            if(stateAbbr != null && !stateAbbr.isEmpty()) return stateAbbr;
            else return null;
        }

        public String getPostalCode() {
            if(postalCode != null && !postalCode.isEmpty()) return postalCode;
            else return null;
        }

        public String getCountry() {
            if(country != null && !country.isEmpty()) return country;
            else return null;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

    }

    public String getId() {
        if(id != null && !id.isEmpty()) return id;
        return null;
    }

    public String getTitle() {
        if(title != null && !title.isEmpty()) return title;
        else return null;
    }

    public String getDescription() {
        if(description != null && !description.isEmpty()) return description;
        else return null;
    }

    public String getCid() {
        if(cid != null && !cid.isEmpty()) return cid;
        else return null;
    }

    public String getBuildingID() {
        if(buildingID != null && !buildingID.isEmpty()) return buildingID;
        else return null;
    }

    public String getBuildingNumber() {
        if(buildingNumber != null && !buildingNumber.isEmpty()) return buildingNumber;
        else return null;
    }

    public String getBuildingCode() {
        if(buildingCode != null && !buildingCode.isEmpty()) return buildingCode;
        else return null;
    }

    public String getCampusCode() {
        if(campusCode != null && !campusCode.isEmpty()) return campusCode;
        else return null;
    }

    public String getCampusName() {
        if(campusName != null && !campusName.isEmpty()) return campusName;
        else return null;
    }

    public List<String> getOffices() {
        if(offices != null && !offices.isEmpty()) return offices;
        else return null;
    }

    public Location getLocation() {
        return location;
    }

}
