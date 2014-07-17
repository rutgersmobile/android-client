package edu.rutgers.css.Rutgers.api;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Promise;
import org.json.JSONArray;

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

}
