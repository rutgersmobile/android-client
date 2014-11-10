package edu.rutgers.css.Rutgers.channels.recreation.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Rutgers campus with recreation facilities. Construct with GSON.
 */
public class Campus {

    private String title;
    private List<Facility> facilities;

    public String getTitle() {
        return title;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    /**
     * Get a facility by title.
     * @param facilityTitle Name of the facility to get
     * @return Facility with matching title if found, null if not.
     */
    public Facility getFacility(@NonNull String facilityTitle) {
        if(facilities == null) return null;

        for(Facility facility: facilities) {
            if(facilityTitle.equalsIgnoreCase(facility.getTitle())) return facility;
        }

        return null;
    }

}
