package edu.rutgers.css.Rutgers.api.model.soc;

import android.support.annotation.NonNull;

/**
 * Subject/department name and code. Construct with GSON.
 */
public class Subject implements Registerable, Comparable<Subject> {
    private final String description;
    private final String code;

    public Subject(final String description, final String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getCode() {
        return code;
    }

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
