package edu.rutgers.css.Rutgers.api.soc.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import edu.rutgers.css.Rutgers.api.soc.Registerable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * Course from Schedule of Classes. Construct with GSON.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Course implements Registerable, Comparable<Course>, Serializable {

    private final String title;
    private final String subject;
    private final String courseNumber;
    @Setter(AccessLevel.NONE) private String courseDescription;
    @Setter(AccessLevel.NONE) private String preReqNotes;
    @Setter(AccessLevel.NONE) private String synopsisUrl;
    @Setter(AccessLevel.NONE) private float credits;
    @Setter(AccessLevel.NONE) private List<Section> sections;
    @Setter(AccessLevel.NONE) private boolean stub;

    /** Create course stub. */
    public Course(String title, String subjectCode, String courseNumber) {
        this.title = title;
        this.subject = subjectCode;
        this.courseNumber = courseNumber;
        this.stub = true;
    }

    @Override
    public String getCode() {
        return getCourseNumber();
    }

    /**
     * Get display string for a course
     * @return Display string, e.g. "112: DATA STRUCTURES"
     */
    @Override
    public String getDisplayTitle() {
        return getTitle() + " (" + getSubject() + ":" + getCourseNumber() + ")";
    }

    /**
     * Get number of open sections for this course.
     * @param hidden Include unlisted sections
     * @return Number of open sections for this course
     */
    public int countOpenSections(boolean hidden) {
        int result = 0;

        for (Section section: sections) {
            if (section.isOpen()) {
                if (hidden || "Y".equalsIgnoreCase(section.getPrinted())) {
                    result++;
                }
            }
        }

        return result;
    }

    /**
     * Get total number of sections for this course.
     * @param hidden Include unlisted sections
     * @return Total number of sections for this course
     */
    public int countTotalSections(boolean hidden) {
        int result = 0;

        for (Section section: sections) {
            if (hidden || "Y".equalsIgnoreCase(section.getPrinted())) {
                result++;
            }
        }

        return result;
    }

    @Override
    public int compareTo(@NonNull Course course) {
        return this.getCode().compareTo(course.getCode());
    }
}
