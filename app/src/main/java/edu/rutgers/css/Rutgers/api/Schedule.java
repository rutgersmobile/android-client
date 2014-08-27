package edu.rutgers.css.Rutgers.api;

import android.os.Bundle;
import android.util.Log;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.android.DeferredAsyncTask;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.items.SOCIndex;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Created by jamchamb on 7/17/14.
 *
 * Regular courses:
 *  http://sis.rutgers.edu/soc/subjects.json?semester=72014&campus=NB&level=U
 *  http://sis.rutgers.edu/soc/courses.json?subject=010&semester=72014&campus=NB&level=U
 *  http://sis.rutgers.edu/soc/course.json?subject=010&semester=72014&campus=NB&courseNumber=272
 * Online courses:
 *  http://sis.rutgers.edu/soc/onlineSubjects.json?term=7&year=2014&level=U
 *  http://sis.rutgers.edu/soc/onlineCourses.json?term=7&year=2014&level=U&subject=010
 *  http://sis.rutgers.edu/soc/onlineCourse.json?term=7&year=2014&level=U&subject=010&courseNumber=372
 */
public class Schedule {

    private static final String TAG = "ScheduleAPI";
    private static final String SOC_BASE_URL = "https://sis.rutgers.edu/soc/";
    private static final String WEBREG_BASE_URL = "https://sims.rutgers.edu/webreg/";

    // Campus codes
    public static final String CODE_CAMPUS_NB = "NB";
    public static final String CODE_CAMPUS_NWK = "NK";
    public static final String CODE_CAMPUS_CAM = "CM";
    public static final String CODE_CAMPUS_ONLINE = "ONLINE";

    // Course levels
    public static final String CODE_LEVEL_UNDERGRAD = "U";
    public static final String CODE_LEVEL_GRAD = "G";

    private static class CallbackResults<T> {
        T result;
        AjaxStatus status;
    }

    protected static AndroidDeferredManager dm = new AndroidDeferredManager();

    /**
     * Get current semester configuration from API.
     * @return SOC Conf API with semesters array and default semester setting
     */
    public static Promise<JSONObject, AjaxStatus, Double> getSemesters() {
        return Request.api("soc_conf.txt", Request.CACHE_ONE_DAY);
    }

    /**
     * Get course subjects
     * @param campus Campus code (e.g. NB)
     * @param level Level code (e.g. U for undergrad, G for graduate)
     * @param semester Semester code (e.g. 72014 for Summer 2014)
     * @return Array of course subjects
     */
    public static Promise<JSONArray, AjaxStatus, Double> getSubjects(String campus, String level, String semester) {
        // Use special online course URL
        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campus)) {
            reqUrl = SOC_BASE_URL + "onlineSubjects.json?term=" + semester.charAt(0) + "&year=" + semester.substring(1) + "&level=" + level;
        }
        else {
            reqUrl = SOC_BASE_URL + "subjects.json?semester=" + semester + "&campus=" + campus + "&level=" + level;
        }
        return Request.jsonArray(reqUrl, Request.CACHE_ONE_DAY);
    }

    /**
     * Get course information for a subject
     * @param campus Campus code (e.g. NB)
     * @param level Level code (e.g. U for undergrad, G for graduate)
     * @param semester Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @return Array of courses for a subject
     */
    public static Promise<JSONArray, AjaxStatus, Double> getCourses(String campus, String level, String semester, String subjectCode) {
        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campus)) {
            reqUrl = SOC_BASE_URL + "onlineCourses.json?term=" + semester.charAt(0) + "&year=" + semester.substring(1) + "&level=" + level + "&subject=" + subjectCode;
        }
        else {
            reqUrl = SOC_BASE_URL + "courses.json?semester=" + semester + "&campus=" + campus + "&level=" + level + "&subject=" + subjectCode;
        }
        return Request.jsonArray(reqUrl, Request.CACHE_ONE_DAY);
    }

    /**
     * Get information for a specific course
     * @param campus Campus code (e.g. NB)
     * @param semester Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @param courseCode Course code (e.g. 101, 252, 344)
     * @return JSON Object for one course
     */
    public static Promise<JSONObject, AjaxStatus, Double> getCourse(String campus, String semester, String subjectCode, String courseCode) {
        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campus)) {
            reqUrl = SOC_BASE_URL + "onlineCourse.json?term=" + semester.charAt(0) + "&year=" + semester.substring(1) + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        }
        else {
            reqUrl = SOC_BASE_URL + "course.json?semester=" + semester + "&campus=" + campus + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        }
        return Request.json(reqUrl, Request.CACHE_ONE_DAY);
    }

    public static Promise<SOCIndex, AjaxStatus, Double> getIndex(final String semesterCode, final String campusCode, final String levelCode) {
        final Deferred<SOCIndex, AjaxStatus, Double> deferred = new DeferredObject<SOCIndex, AjaxStatus, Double>();

        dm.when(new DeferredAsyncTask<Void, Double, CallbackResults<SOCIndex>>() {
            @Override
            protected CallbackResults<SOCIndex> doInBackgroundSafe(Void... nil) throws Exception {
                AjaxCallback<JSONObject> cb = Request.apiSynchronous("indexes/" + semesterCode + "_" + campusCode + "_" + levelCode + ".json", Request.CACHE_ONE_DAY);

                AjaxStatus status = cb.getStatus();
                JSONObject result = cb.getResult();

                CallbackResults results = new CallbackResults();

                results.status = status;

                if(result == null || status.getCode() == AjaxStatus.TRANSFORM_ERROR) {
                    Log.e(TAG, "getIndex(): " + AppUtil.formatAjaxStatus(status));
                    results.result = null;
                }
                else {
                    SOCIndex index = new SOCIndex(result);
                    results.result = index;
                }

                return results;
            }
        }).done(new DoneCallback<CallbackResults<SOCIndex>>() {
            @Override
            public void onDone(CallbackResults<SOCIndex> results) {
                if(results.result == null) deferred.reject(results.status);
                else deferred.resolve(results.result);
            }
        });

        return deferred.promise();
    }

    /**
     * Convert a semester code (e.g. "72014") to human-readable form ("Summer 2014")
     * @param semesterCode Semester code
     * @return Human-readable semester name
     */
    public static String translateSemester(String semesterCode) {
        if(semesterCode == null) return null;
        if(semesterCode.length() != 5) {
            return invalidSemester(semesterCode);
        }

        int leadingDigit = Character.getNumericValue(semesterCode.charAt(0));
        if(leadingDigit < 0) {
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
        Log.e(TAG, "Invalid semester code \"" + semesterCode + "\"");
        return semesterCode;
    }

    /**
     * Get the number of open visible sections, as well as the total number of visible sections.
     * @param courseJSON Course to count visible sections for
     * @return Int array with index 0 being the number of open sections, and index 1 being the total
     */
    public static int[] countVisibleSections(JSONObject courseJSON) {
        int count = 0;
        int openCount = 0;

        if(courseJSON != null) {
            try {
                JSONArray sections = courseJSON.getJSONArray("sections");
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject section = sections.getJSONObject(i);
                    if (section.getString("printed").equalsIgnoreCase("Y")) {
                        if (section.getBoolean("openStatus")) openCount++;
                        count++;
                    }
                }
            } catch (JSONException e) {
                Log.w(TAG, "countVisibleOpenSections(): " + e.getMessage());
            }
        }

        int[] result = new int[2];
        result[0] = openCount;
        result[1] = count;

        return result;
    }

    /**
     * Open WebReg registration for this course section in the browser.
     * @param semester Semester code (e.g. 72014 for Summer 2014)
     * @param index Section index number
     */
    public static void openRegistrationWindow(String semester, String index) {
        String url = WEBREG_BASE_URL + "editSchedule.htm?login=cas&semesterSelection=" + semester + "&indexList=" + index;

        Bundle args = new Bundle();
        args.putString("component", WebDisplay.HANDLE);
        args.putString("url", url);

        ComponentFactory.getInstance().switchFragments(args);
    }

    public static String courseLine(JSONObject jsonObject) {
        return jsonObject.optString("courseNumber") + ": " +jsonObject.optString("title");
    }

    public static String subjectLine(JSONObject subjectJSON) {
        return subjectJSON.optString("description") + " (" + subjectJSON.optString("code") + ")";
    }

}
