package edu.rutgers.css.Rutgers.api.recreation.model;

import android.support.annotation.NonNull;

import java.util.List;

import edu.rutgers.css.Rutgers.api.recreation.model.facility.Facility;

/**
 * Rutgers campus with recreation facilities. Construct with GSON.
 */
public class Campus {

    private final String title;
    private final List<Facility> facilities;

    public Campus(final String title, final List<Facility> facilities) {
        this.title = title;
        this.facilities = facilities;
    }

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
        if (facilities == null) return null;

        for (Facility facility: facilities) {
            if (facilityTitle.equalsIgnoreCase(facility.getTitle())) return facility;
        }

        return null;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
