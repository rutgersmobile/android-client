package edu.rutgers.css.Rutgers.channels.soc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jamchamb on 10/30/14.
 */
public class Section extends SectionAdapterItem implements Serializable {

    private String subtopic;
    private String subtitle;
    private String index;
    private String number;
    private String examCode;
    private String printed;
    @SerializedName("openStatus") private boolean open;
    private String sectionNotes;
    private String sessionDates;
    private String sessionDatePrintIndicator;
    private String campusCode;
    private List<Instructor> instructors;
    private List<MeetingTime> meetingTimes;

    @Override
    public String getDisplayTitle() {
        if (open) return "Open Section";
        else return "Closed Section";
    }

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

    public List<MeetingTime> getMeetingTimes() {
        return meetingTimes;
    }

    public String getSubtopic() {
        return subtopic;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getIndex() {
        return index;
    }

    public String getNumber() {
        return number;
    }

    public String getExamCode() {
        return examCode;
    }

    public String getSessionDates() {
        return sessionDates;
    }

    public String getSessionDatePrintIndicator() {
        return sessionDatePrintIndicator;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public static class Instructor implements Serializable {
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class MeetingTime implements Serializable {
        private String meetingDay;
        private String meetingModeDesc;
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

        public String getMeetingModeDesc() {
            return meetingModeDesc;
        }
    }

}