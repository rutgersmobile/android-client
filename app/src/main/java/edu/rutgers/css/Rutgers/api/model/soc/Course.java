package edu.rutgers.css.Rutgers.api.model.soc;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * Course from Schedule of Classes. Construct with GSON.
 */
public class Course implements Registerable, Comparable<Course>, Serializable {

    private final String title;
    private final String subject;
    private final String courseNumber;
    private String courseDescription;
    private String preReqNotes;
    private String synopsisUrl;
    private float credits;
    private List<Section> sections;
    private boolean stub;

    /** Create course stub. */
    public Course(String title, String subjectCode, String courseNumber) {
        this.title = title;
        this.subject = subjectCode;
        this.courseNumber = courseNumber;
        this.stub = true;
    }

    /**
     * Readable course name
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Subject that the course is in, ex. "198" for Computer Science
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Number of course, ex. "112" for Data Structures
     */
    public String getCourseNumber() {
        return courseNumber;
    }

    /**
     * Readable description of course
     */
    public String getCourseDescription() {
        return courseDescription;
    }

    /**
     * Readable description of courses needed to take this course
     */
    public String getPreReqNotes() {
        return preReqNotes;
    }

    public String getSynopsisUrl() {
        return synopsisUrl;
    }

    /**
     * Number of credits course is worth
     */
    public float getCredits() {
        return credits;
    }

    /**
     * All sections with meeting times, instructors, closed/open status and more
     */
    public List<Section> getSections() {
        return sections;
    }

    public boolean isStub() {
        return stub;
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
