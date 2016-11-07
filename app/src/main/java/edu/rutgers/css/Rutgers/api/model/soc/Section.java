package edu.rutgers.css.Rutgers.api.model.soc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jamchamb on 10/30/14.
 */
public class Section implements Titleable, Serializable {

    private final String subtopic;
    private final String subtitle;
    private final String index;
    private final String number;
    private final String examCode;
    private final String printed;
    @SerializedName("openStatus") private final boolean open;
    private final String sectionNotes;
    private final String sessionDates;
    private final String sessionDatePrintIndicator;
    private final String campusCode;
    private final List<Instructor> instructors;
    private final List<MeetingTime> meetingTimes;

    public Section(final String subtopic, final String subtitle, final String index, final String number,
                   final String examCode, final String printed, final boolean open, final String sectionNotes,
                   final String sessionDates, final String sessionDatePrintIndicator, final String campusCode,
                   final List<Instructor> instructors, final List<MeetingTime> meetingTimes) {
        this.subtopic = subtopic;
        this.subtitle = subtitle;
        this.index = index;
        this.number = number;
        this.examCode = examCode;
        this.printed = printed;
        this.open = open;
        this.sectionNotes = sectionNotes;
        this.sessionDates = sessionDates;
        this.sessionDatePrintIndicator = sessionDatePrintIndicator;
        this.campusCode = campusCode;
        this.instructors = instructors;
        this.meetingTimes = meetingTimes;
    }

    public static class Instructor implements Serializable {
        private final String name;
        public Instructor(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class MeetingTime implements Serializable {
        private final String meetingDay;
        private final String meetingModeDesc;
        private final String startTime;
        private final String endTime;
        private final String pmCode;
        private final String campusAbbrev;
        private final String buildingCode;
        private final String roomNumber;

        public MeetingTime(final String meetingDay, final String meetingModeDesc, final String startTime,
                           final String endTime, final String pmCode, final String campusAbbrev,
                           final String buildingCode, final String roomNumber) {
            this.meetingDay = meetingDay;
            this.meetingModeDesc = meetingModeDesc;
            this.startTime = startTime;
            this.endTime = endTime;
            this.pmCode = pmCode;
            this.campusAbbrev = campusAbbrev;
            this.buildingCode = buildingCode;
            this.roomNumber = roomNumber;
        }

        public String getMeetingDay() {
            return meetingDay;
        }

        public String getMeetingModeDesc() {
            return meetingModeDesc;
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

    @Override
    public String getDisplayTitle() {
        if (open) return "Open Section";
        else return "Closed Section";
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

    public String getPrinted() {
        return printed;
    }

    public boolean isOpen() {
        return open;
    }

    public String getSectionNotes() {
        return sectionNotes;
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

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public List<MeetingTime> getMeetingTimes() {
        return meetingTimes;
    }
}