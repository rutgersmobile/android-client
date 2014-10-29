package edu.rutgers.css.Rutgers.api;

import android.os.Bundle;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.items.SOCIndex;
import edu.rutgers.css.Rutgers.items.Schedule.Course;
import edu.rutgers.css.Rutgers.items.Schedule.Semesters;
import edu.rutgers.css.Rutgers.items.Schedule.Subject;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Schedule of Classes API
 */
public class Schedule {

    private static final String TAG = "ScheduleAPI";
    private static final String SOC_BASE_URL = "https://sis.rutgers.edu/soc/";
    private static final String WEBREG_BASE_URL = "https://sims.rutgers.edu/webreg/";

    // Campus codes (full list should be read from res/raw/soc_campuses.json)
    public static final String CODE_CAMPUS_NB = "NB";
    public static final String CODE_CAMPUS_NWK = "NK";
    public static final String CODE_CAMPUS_CAM = "CM";
    public static final String CODE_CAMPUS_ONLINE = "ONLINE";

    // Course levels
    public static final String CODE_LEVEL_UNDERGRAD = "U";
    public static final String CODE_LEVEL_GRAD = "G";

    /**
     * Get current semester configuration from API.
     * @return SOC Conf API with semesters array and default semester setting
     */
    public static Promise<Semesters, Exception, Void> getSemesters() {
        final DeferredObject<Semesters, Exception, Void> deferred = new DeferredObject<Semesters, Exception, Void>();

        Request.api("soc_conf.txt", Request.CACHE_ONE_DAY).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                Gson gson = new Gson();
                try {
                    deferred.resolve(gson.fromJson(result.toString(), Semesters.class));
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "getSemesters(): " + e.getMessage());
                    deferred.reject(e);
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }
        });

        return deferred.promise();
    }

    /**
     * Get course subjects
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @return Promise for array of course subjects
     */
    public static Promise<List<Subject>, Exception, Void> getSubjects(String campusCode, String levelCode, String semesterCode) {
        final DeferredObject<List<Subject>, Exception, Void> deferred = new DeferredObject<List<Subject>, Exception, Void>();

        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineSubjects.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&level=" + levelCode;
        } else {
            reqUrl = SOC_BASE_URL + "subjects.json?semester=" + semesterCode + "&campus=" + campusCode + "&level=" + levelCode;
        }

        Request.jsonArray(reqUrl, Request.CACHE_ONE_DAY).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                Gson gson = new Gson();
                ArrayList<Subject> subjects = new ArrayList<Subject>(result.length());

                try {
                    for(int i = 0; i < result.length(); i++) {
                        Subject newSub = gson.fromJson(result.getJSONObject(i).toString(), Subject.class);
                        subjects.add(newSub);
                    }
                    deferred.resolve(subjects);
                } catch (JSONException e) {
                    Log.e(TAG, "getSubjects(): " + e.getMessage());
                    deferred.reject(e);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "getSubjects(): " + e.getMessage());
                    deferred.reject(e);
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }
        });

        return deferred.promise();
    }

    /**
     * Get all courses for a subject
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @return Array of courses for a subject
     */
    public static Promise<List<Course>, Exception, Void> getCourses(String campusCode, String levelCode, String semesterCode, String subjectCode) {
        final DeferredObject<List<Course>, Exception, Void> deferred = new DeferredObject<List<Course>, Exception, Void>();

        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineCourses.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&level=" + levelCode + "&subject=" + subjectCode;
        } else {
            reqUrl = SOC_BASE_URL + "courses.json?semester=" + semesterCode + "&campus=" + campusCode + "&level=" + levelCode + "&subject=" + subjectCode;
        }

        Request.jsonArray(reqUrl, Request.CACHE_ONE_DAY).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                Gson gson = new Gson();
                ArrayList<Course> courses = new ArrayList<Course>(result.length());

                try {
                    for(int i = 0; i < result.length(); i++) {
                        Course newCourse = gson.fromJson(result.getJSONObject(i).toString(), Course.class);
                        courses.add(newCourse);
                    }
                    deferred.resolve(courses);
                } catch (JSONException e) {
                    Log.e(TAG, "getCourses(): " + e.getMessage());
                    deferred.reject(e);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "getCourses(): " + e.getMessage());
                    deferred.reject(e);
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }
        });

        return deferred.promise();
    }

    /**
     * Get information for a specific course
     * @param campusCode Campus code (e.g. NB)
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param subjectCode Subject code (e.g. 010, 084)
     * @param courseCode Course code (e.g. 101, 252, 344)
     * @return JSON Object for one course
     */
    public static Promise<Course, Exception, Void> getCourse(String campusCode, String semesterCode, String subjectCode, String courseCode) {
        final DeferredObject<Course, Exception, Void> deferred = new DeferredObject<Course, Exception, Void>();

        String reqUrl;
        if(CODE_CAMPUS_ONLINE.equals(campusCode)) {
            reqUrl = SOC_BASE_URL + "onlineCourse.json?term=" + semesterCode.charAt(0) + "&year=" + semesterCode.substring(1) + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        } else {
            reqUrl = SOC_BASE_URL + "course.json?semester=" + semesterCode + "&campus=" + campusCode + "&subject=" + subjectCode + "&courseNumber=" + courseCode;
        }

        Request.json(reqUrl, Request.CACHE_ONE_DAY).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                Gson gson = new Gson();
                try {
                    deferred.resolve(gson.fromJson(result.toString(), Course.class));
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "getCourse(): " + e.getMessage());
                    deferred.reject(e);
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }
        });

        return deferred.promise();
    }

    /**
     * Get SOC Index file for a schedule
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param campusCode Campus code (e.g. NB)
     * @param levelCode Level code (e.g. U for undergrad, G for graduate)
     * @return Promise for an SOCIndex
     */
    public static Promise<SOCIndex, Exception, Double> getIndex(final String semesterCode, final String campusCode, final String levelCode) {
        final Deferred<SOCIndex, Exception, Double> deferred = new DeferredObject<SOCIndex, Exception, Double>();

        Request.api("indexes/"+semesterCode+"_"+campusCode+"_"+levelCode+".json", Request.CACHE_ONE_DAY).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                try {
                    SOCIndex socIndex = new SOCIndex(campusCode, levelCode, semesterCode, result);
                    deferred.resolve(socIndex);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "getIndex(): " + e.getMessage());
                    deferred.reject(e);
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
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
     * Open WebReg registration for this course section in the browser.
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param courseIndex Section index number
     */
    public static void openRegistrationWindow(String semesterCode, String courseIndex) {
        String url = WEBREG_BASE_URL + "editSchedule.htm?login=cas&semesterSelection=" + semesterCode + "&indexList=" + courseIndex;

        Bundle args = new Bundle();
        args.putString("component", WebDisplay.HANDLE);
        args.putString("url", url);

        ComponentFactory.getInstance().switchFragments(args);
    }

}
