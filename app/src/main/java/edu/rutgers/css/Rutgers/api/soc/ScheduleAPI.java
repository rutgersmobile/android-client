package edu.rutgers.css.Rutgers.api.soc;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.api.soc.model.Semesters;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import edu.rutgers.css.Rutgers.api.soc.parsers.SOCIndexDeserializer;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Schedule of Classes API
 */
public final class ScheduleAPI {

    private static final String TAG = "ScheduleAPI";
    // TODO should be https but it doesn't have a SHA-2 cert
    private static final String SOC_BASE_URL = "http://sis.rutgers.edu/soc/";
    private static final String WEBREG_BASE_URL = "https://sims.rutgers.edu/webreg/";

    // Campus codes (full list should be read from res/raw/soc_campuses.json)
    public static final String CODE_CAMPUS_NB = "NB";
    public static final String CODE_CAMPUS_NWK = "NK";
    public static final String CODE_CAMPUS_CAM = "CM";
    public static final String CODE_CAMPUS_ONLINE = "ONLINE";

    // Course levels
    public static final String CODE_LEVEL_UNDERGRAD = "U";
    public static final String CODE_LEVEL_GRAD = "G";

    private ScheduleAPI() {}

    /**
     * Get current semester configuration from API.
     * @return SOC Conf API with semesters array and default semester setting
     */
    public static Semesters getSemesters() throws JsonSyntaxException, IOException {
        return ApiRequest.api("soc_conf.txt", 1, TimeUnit.DAYS, Semesters.class);
    }

    /**
     * Get course subjects
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @return Promise for array of course subjects
     */
    public static List<Subject> getSubjects(String campusCode, String levelCode, String semesterCode) throws JsonSyntaxException, IOException {
        String reqUrl;
        if (CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineSubjects.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&level=" + levelCode;
        } else {
            reqUrl = SOC_BASE_URL + "subjects.json?semester=" + semesterCode + "&campus=" + campusCode + "&level=" + levelCode;
        }

        Type type = new TypeToken<List<Subject>>(){}.getType();
        return ApiRequest.json(reqUrl, 1, TimeUnit.DAYS, type);
    }

    /**
     * Get all courses for a subject
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @return Array of courses for a subject
     */
    public static List<Course> getCourses(String campusCode, String levelCode, String semesterCode, String subjectCode) throws JsonSyntaxException, IOException {
        String reqUrl;
        if (CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineCourses.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&level=" + levelCode + "&subject=" + subjectCode;
        } else {
            reqUrl = SOC_BASE_URL + "courses.json?semester=" + semesterCode + "&campus=" + campusCode + "&level=" + levelCode + "&subject=" + subjectCode;
        }

        Type type = new TypeToken<List<Course>>(){}.getType();
        return ApiRequest.json(reqUrl, 1, TimeUnit.DAYS, type);
    }

    /**
     * Get information for a specific course
     * @param campusCode Campus code (e.g. NB)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @param courseCode Course code (e.g. 101, 252, 344)
     * @return JSON Object for one course
     */
    public static Course getCourse(String campusCode, String semesterCode, String subjectCode, String courseCode) throws JsonSyntaxException, IOException {
        String reqUrl;
        if (CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineCourse.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        } else {
            reqUrl = SOC_BASE_URL + "course.json?semester=" + semesterCode + "&campus=" + campusCode + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        }

        return ApiRequest.json(reqUrl, 1, TimeUnit.DAYS, Course.class);
    }

    /**
     * Get SOC Index file for a schedule
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @return Promise for an SOCIndex
     */
    public static SOCIndex getIndex(final String semesterCode, final String campusCode, final String levelCode) throws JsonSyntaxException, IOException {
        String resource = "indexes/" + semesterCode + "_" + campusCode + "_" + levelCode + ".json";
        SOCIndex index = ApiRequest.api(resource, 1, TimeUnit.DAYS, SOCIndex.class, new SOCIndexDeserializer());
        index.setCampusCode(campusCode);
        index.setLevelCode(levelCode);
        index.setSemesterCode(semesterCode);
        return index;
    }

    /**
     * Convert a semester code (e.g. "72014") to human-readable form ("Summer 2014")
     * @param semesterCode Semester code
     * @return Human-readable semester name
     */
    public static String translateSemester(String semesterCode) {
        if (semesterCode == null) return null;
        if (semesterCode.length() != 5) {
            return invalidSemester(semesterCode);
        }

        int leadingDigit = Character.getNumericValue(semesterCode.charAt(0));
        if (leadingDigit < 0) {
            return invalidSemester(semesterCode);
        }

        StringBuilder result = new StringBuilder();

        switch(leadingDigit) {
            case 0:
                result.append("Winter");
                break;
            case 1:
                result.append("Spring");
                break;
            case 7:
                result.append("Summer");
                break;
            case 9:
                result.append("Fall");
                break;
            default:
                return invalidSemester(semesterCode);
        }

        result.append(" ");
        result.append(semesterCode.substring(1));

        return result.toString();
    }

    /**
     * Log an invalid semester code and return the string as is.
     * @param semesterCode Semester code
     * @return Unchanged invalid string
     */
    private static String invalidSemester(String semesterCode) {
        LOGE(TAG, "Invalid semester code \"" + semesterCode + "\"");
        return semesterCode;
    }

    /**
     * Open WebReg registration for this course section in the browser.
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param courseIndex Section index number
     */
    public static String getRegistrationLink(String semesterCode, String courseIndex) {
        return WEBREG_BASE_URL + "editSchedule.htm?login=cas&semesterSelection=" + semesterCode + "&indexList=" + courseIndex;
    }

}
