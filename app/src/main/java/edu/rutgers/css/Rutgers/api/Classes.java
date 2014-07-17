package edu.rutgers.css.Rutgers.api;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Promise;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by jamchamb on 7/17/14.
 */
public class Classes {

    private static final String TAG = "Classes";
    private static final String BASE_URL = "https://sis.rutgers.edu/soc/";

    public static Promise<JSONArray, AjaxStatus, Double> getSubjects(String campus, String level, String semester) {
        String reqUrl = BASE_URL + "subjects.json?semester=" + semester + "&campus=" + campus + "&level=" + level;
        return Request.jsonArray(reqUrl, Request.CACHE_ONE_HOUR);
    }

    public static Promise<JSONArray, AjaxStatus, Double> getCourses(String campus, String level, String semester, String subjectCode) {
        String reqUrl = BASE_URL + "courses.json?semester=" + semester + "&campus=" + campus + "&level=" + level + "&subject=" + subjectCode;
        return Request.jsonArray(reqUrl, Request.CACHE_ONE_HOUR);
    }

    public static Promise<JSONObject, AjaxStatus, Double> getCourse(String campus, String semester, String subjectCode, String courseCode) {
        String reqUrl = BASE_URL + "course.json?semester=" + semester + "&campus=" + campus + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        return Request.json(reqUrl, Request.CACHE_ONE_HOUR);
    }

}
