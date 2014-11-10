package edu.rutgers.css.Rutgers.channels.soc.model;

/**
 * Subject/department name and code. Construct with GSON.
 */
public class Subject extends ScheduleAdapterItem {
    private String description;
    private String code;

    public Subject(String description, String code) {
        this.description = description;
        this.code = code;
    }

    /**
     * Get display string for a subject
     * @return Display string, e.g. "COMPUTER SCIENCE (198)"
     */
    @Override
    public String getDisplayTitle() {
        return getDescription() + " (" + getCode() + ")";
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }
}
