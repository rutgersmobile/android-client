package edu.rutgers.css.Rutgers.api.places.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents a building from the places database. Equivalent to a "place" JSON object from the
 * places API. Use GSON to construct.
 * @author James Chambers
 */
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

    public Place(final String id, final String title, final String description, final String cid,
                 final String buildingID, final String buildingNumber, final String buildingCode,
                 final String campusCode, final String campusName, final List<String> offices,
                 final Location location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cid = cid;
        this.buildingID = buildingID;
        this.buildingNumber = buildingNumber;
        this.buildingCode = buildingCode;
        this.campusCode = campusCode;
        this.campusName = campusName;
        this.offices = offices;
        this.location = location;
    }

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

        public Location(final String name, final String street, final String additional,
                        final String city, final String state, final String stateAbbr,
                        final String postalCode, final String country, final double latitude, final double longitude) {
            this.name = name;
            this.street = street;
            this.additional = additional;
            this.city = city;
            this.state = state;
            this.stateAbbr = stateAbbr;
            this.postalCode = postalCode;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
        }

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

    @Override
    public String toString() {
        return getTitle();
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
}
