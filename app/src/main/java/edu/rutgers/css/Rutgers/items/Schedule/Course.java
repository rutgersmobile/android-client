package edu.rutgers.css.Rutgers.items.Schedule;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Course from Schedule of Classes. Construct with GSON.
 */
public class Course extends ScheduleAdapterItem {

    private String title;
    private String subject;
    private String courseNumber;
    private String courseDescription;
    private String preReqNotes;
    private int credits;
    private List<Section> sections;
    private List<MeetingTime> meetingTimes;
    private boolean stub;

    /** Create course stub. */
    public Course(String title, String subjectCode, String courseNumber) {
        this.title = title;
        this.subject = subjectCode;
        this.courseNumber = courseNumber;
        this.stub = true;
    }

    /** Used for filling in stub course info. */
    public void updateFields(@NonNull Course other) {
        if(other.isStub()) throw new IllegalArgumentException("Can't update using a stub course");
        this.title = other.getTitle();
        this.subject = other.getSubject();
        this.courseNumber = other.getCourseNumber();
        this.courseDescription = other.getCourseDescription();
        this.preReqNotes = other.getPreReqNotes();
        this.credits = other.getCredits();
        this.sections = other.getSections();
        this.meetingTimes = other.getMeetingTimes();
        this.stub = false;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Get display string for a course
     * @return Display string, e.g. "112: DATA STRUCTURES"
     */
    @Override
    public String getDisplayTitle() {
        return getCourseNumber() + ": " + getTitle();
    }

    /**
     * Get number of open sections for this course.
     * @param hidden Include unlisted sections
     * @return Number of open sections for this course
     */
    public int countOpenSections(boolean hidden) {
        int result = 0;

        for(Section section: sections) {
            if(section.isOpen()) {
                if(hidden || "Y".equalsIgnoreCase(section.getPrinted())) {
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

        for(Section section: sections) {
            if(hidden || "Y".equalsIgnoreCase(section.getPrinted())) {
                result++;
            }
        }

        return result;
    }

    public boolean isStub() {
        return stub;
    }

    public String getSubject() {
        return subject;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public String getPreReqNotes() {
        return preReqNotes;
    }

    public int getCredits() {
        return credits;
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<MeetingTime> getMeetingTimes() {
        return meetingTimes;
    }

    public static class Section {
        private String printed;
        @SerializedName("openStatus") private boolean open;
        private String sectionNotes;
        private List<Instructor> instructors;

        public String getPrinted() {
            return printed;
        }

        public boolean isOpen() {
            return open;
        }

        public String getSectionNotes() {
            return sectionNotes;
        }

        public List<Instructor> getInstructors() {
            return instructors;
        }
    }

    public static class Instructor {
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class MeetingTime {
        private String meetingDay;
        private String startTime;
        private String endTime;
        private String pmCode;
        private String campusAbbrev;
        private String buildingCode;
        private String roomNumber;

        public String getMeetingDay() {
            return meetingDay;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getPmCode() {
            return pmCode;
        }

        public String getCampusAbbrev() {
            return campusAbbrev;
        }

        public String getBuildingCode() {
            return buildingCode;
        }

        public String getRoomNumber() {
            return roomNumber;
        }
    }

}
