package edu.rutgers.css.Rutgers.api.soc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import edu.rutgers.css.Rutgers.api.soc.Titleable;
import lombok.Data;

/**
 * Created by jamchamb on 10/30/14.
 */
@Data
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

    @Override
    public String getDisplayTitle() {
        if (open) return "Open Section";
        else return "Closed Section";
    }

    @Data
    public static class Instructor implements Serializable {
        private final String name;
    }

    @Data
    public static class MeetingTime implements Serializable {
        private final String meetingDay;
        private final String meetingModeDesc;
        private final String startTime;
        private final String endTime;
        private final String pmCode;
        private final String campusAbbrev;
        private final String buildingCode;
        private final String roomNumber;
    }
}