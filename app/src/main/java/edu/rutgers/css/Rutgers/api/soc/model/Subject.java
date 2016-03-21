package edu.rutgers.css.Rutgers.api.soc.model;

import android.support.annotation.NonNull;

import edu.rutgers.css.Rutgers.api.soc.Registerable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Subject/department name and code. Construct with GSON.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Subject implements Registerable, Comparable<Subject> {
    private final String description;
    private final String code;

    /**
     * Get display string for a subject
     * @return Display string, e.g. "COMPUTER SCIENCE (198)"
     */
    @Override
    public String getDisplayTitle() {
        return getDescription() + " (" + getCode() + ")";
    }

    @Override
    public String getTitle() {
        return getDescription();
    }

    @Override
    public int compareTo(@NonNull Subject subject) {
        return this.getTitle().compareTo(subject.getTitle());
    }
}
