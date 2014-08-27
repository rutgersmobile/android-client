package edu.rutgers.css.Rutgers.items;

import android.util.Log;

import com.androidquery.callback.AjaxCallback;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers.utils.JsonUtil;

/**
 * Created by jamchamb on 8/21/14.
 */
public class SOCIndex {

    private static final String TAG = "SOCIndex";

    private static class Course {
        String course;
        String subj;
    }

    private static class Subject {
        String id;
        String name;
        HashMap<String, String> courses;
    }

    private String semesterCode;
    private String campusCode;
    private String levelCode;

    private HashMap<String, String[]> mAbbreviations;
    private HashMap<String, Course> mCourses;
    private HashMap<String, Subject> mSubjects;
    private HashMap<String, String> mSubjectNames;

    public SOCIndex(String campusCode, String levelCode, String semesterCode, JSONObject index) {
        if(!(index.has("abbrevs") && index.has("courses") && index.has("ids") && index.has("names"))) {
            throw new IllegalArgumentException("Invalid index, missing critical fields");
        }

        setSemesterCode(semesterCode);
        setCampusCode(campusCode);
        setLevelCode(levelCode);

        // Convert the JSON into native hashtables
        try {
            JSONObject abbrevs = index.getJSONObject("abbrevs"); // List of subject abbrevs->sub IDs
            JSONObject ids = index.getJSONObject("ids"); // List of subject IDs->contained courses
            JSONObject names = index.getJSONObject("names"); // List of subject names->sub IDs
            JSONObject courses = index.getJSONObject("courses"); // List of course names->sub/course IDs

            // Set up abbreviations hashtable
            mAbbreviations = new HashMap<String, String[]>();
            Iterator<String> abbrevsIterator = abbrevs.keys();
            while(abbrevsIterator.hasNext()) {
                String curAbbrev = abbrevsIterator.next();
                JSONArray curContents = abbrevs.getJSONArray(curAbbrev);
                String[] subIDStrings = JsonUtil.jsonToStringArray(curContents);
                mAbbreviations.put(curAbbrev, subIDStrings);
            }

            // Set up subject IDs hashtable
            mSubjects = new HashMap<String, Subject>();
            Iterator<String> idsIterator = ids.keys();
            while(idsIterator.hasNext()) {
                String curID = idsIterator.next();
                JSONObject curContents = ids.getJSONObject(curID);

                // Set up the list of CourseID:CourseName mappings for this Subject ID entry
                JSONObject curCourses = curContents.getJSONObject("courses");
                HashMap<String, String> courseMap = new HashMap<String, String>();
                Iterator<String> courseIDIterator = curCourses.keys();
                while(courseIDIterator.hasNext()) {
                    String curCourseID = courseIDIterator.next();
                    String curCourseName = curCourses.getString(curCourseID);
                    courseMap.put(curCourseID, curCourseName);
                }

                Subject newSubject = new Subject();
                newSubject.id = curID;
                newSubject.name = curContents.getString("name");
                newSubject.courses = courseMap;

                mSubjects.put(curID, newSubject);
            }

            // Set up subject names hashtable
            mSubjectNames = new HashMap<String, String>();
            Iterator<String> namesIterator = names.keys();
            while(namesIterator.hasNext()) {
                String curName = namesIterator.next();
                String curContents = names.getString(curName);
                mSubjectNames.put(curName, curContents);
            }

            // Set up course names
            mCourses = new HashMap<String, Course>();
            Iterator<String> coursesIterator = courses.keys();
            while(coursesIterator.hasNext()) {
                String curCourseName = coursesIterator.next();
                JSONObject curContents = courses.getJSONObject(curCourseName);
                Course newCourse = new Course();
                newCourse.course = curContents.getString("course");
                newCourse.subj = curContents.getString("subj");
                mCourses.put(curCourseName, newCourse);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid index JSON: " + e.getMessage());
        }
    }

    /**
     * Get subjects by abbreviation
     * @param abbrev Abbreviation
     * @return List of subject JSON objects (empty if no results found)
     */
    public List<JSONObject> getSubjectsByAbbreviation(String abbrev) {
        List<JSONObject> results = new ArrayList<JSONObject>();
        if(mAbbreviations.containsKey(abbrev)) {
            String[] subjCodes = mAbbreviations.get(abbrev);
            for(String subjCode: subjCodes) {
                Subject curSubject = mSubjects.get(subjCode);
                if(curSubject != null) {
                    try {
                        JSONObject newSubjectJSON = new JSONObject();
                        newSubjectJSON.put("description", curSubject.name.toUpperCase());
                        newSubjectJSON.put("code", curSubject.id);
                        results.add(newSubjectJSON);
                    } catch (JSONException e) {
                        Log.w(TAG, "getSubjectsByAbbreviation(): " + e.getMessage());
                    }
                }
            }
        }

        return results;
    }

    /**
     * Get subject by subject code
     * @param subjectCode Subject code
     * @return Subject JSON object
     */
    public JSONObject getSubjectByCode(String subjectCode) {
        Subject subject = mSubjects.get(subjectCode);
        if(subject == null) return null;

        try {
            JSONObject newSubjectJSON = new JSONObject();
            newSubjectJSON.put("description", subject.name.toUpperCase());
            newSubjectJSON.put("code", subject.id);
            return newSubjectJSON;
        } catch (JSONException e) {
            Log.w(TAG, "getSubjectByCode(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Get course by subject & course code combination
     * @param subjectCode Subject code
     * @param courseCode Course code
     * @return Course-stub JSON object
     */
    public JSONObject getCourseByCode(String subjectCode, String courseCode) {
        AjaxCallback<JSONObject> cb = Schedule.getCourseSynchronous(getCampusCode(), getSemesterCode(), subjectCode, courseCode);
        if(cb.getResult() != null) {
            return cb.getResult();
        }
        else {
            Log.w(TAG, "Failed to get course JSON for " + subjectCode + ":" + courseCode);
            Log.w(TAG, AppUtil.formatAjaxStatus(cb.getStatus()));
            return null;
        }
    }

    /**
     * Get courses by partial title matches on query.
     * @param query Query string
     * @param cap Maximum number of results (cutoff point)
     * @return List of course-stub JSON objects (empty if no results found)
     */
    public List<JSONObject> getCoursesByName(String query, int cap) {
        List<JSONObject> results = new ArrayList<JSONObject>();

        Set<Map.Entry<String, Course>> set = mCourses.entrySet();
        Iterator<Map.Entry<String, Course>> courseIter = set.iterator();
        while(courseIter.hasNext()) {
            Map.Entry<String, Course> curEntry = courseIter.next();
            // If there's a partial match on the full course name...
            if(StringUtils.containsIgnoreCase(curEntry.getKey(), query)) {
                Course curCourse = curEntry.getValue();
                String subjectCode = curCourse.subj;
                String courseCode = curCourse.course;

                AjaxCallback<JSONObject> cb = Schedule.getCourseSynchronous(getCampusCode(), getSemesterCode(), subjectCode, courseCode);
                if(cb.getResult() != null) {
                    results.add(cb.getResult());
                }
                else {
                    Log.w(TAG, "Failed to get course JSON for " + subjectCode + ":" + courseCode);
                    Log.w(TAG, AppUtil.formatAjaxStatus(cb.getStatus()));
                }
            }
        }

        return results;
    }


    public void setSemesterCode(String semesterCode) {
        this.semesterCode = semesterCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public String getSemesterCode() {
        return semesterCode;
    }

    public String getCampusCode() {
        return campusCode;
    }

}
