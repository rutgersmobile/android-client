package edu.rutgers.css.Rutgers.items;

import android.util.Log;

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
    private HashMap<String, Course> mCoursesByName;
    private HashMap<String, Subject> mSubjectsByCode;
    private HashMap<String, String> mSubjectsByName;

    public SOCIndex(String campusCode, String levelCode, String semesterCode, JSONObject index) throws IllegalArgumentException {
        if(index.isNull("abbrevs") || index.isNull("courses") || index.isNull("ids") || index.isNull("names")) {
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
            for(Iterator<String> abbrevsIterator = abbrevs.keys(); abbrevsIterator.hasNext();) {
                String curAbbrev = abbrevsIterator.next();
                JSONArray curContents = abbrevs.getJSONArray(curAbbrev);
                String[] subIDStrings = JsonUtil.jsonToStringArray(curContents);
                mAbbreviations.put(curAbbrev, subIDStrings);
            }

            // Set up subject IDs hashtable
            mSubjectsByCode = new HashMap<String, Subject>();
            for(Iterator<String> idsIterator = ids.keys(); idsIterator.hasNext();) {
                String curID = idsIterator.next();
                JSONObject curContents = ids.getJSONObject(curID);

                // Set up the list of CourseID:CourseName mappings for this Subject ID entry
                JSONObject curCourses = curContents.getJSONObject("courses");
                HashMap<String, String> courseMap = new HashMap<String, String>();
                for(Iterator<String> courseIDIterator = curCourses.keys(); courseIDIterator.hasNext();) {
                    String curCourseID = courseIDIterator.next();
                    String curCourseName = curCourses.getString(curCourseID);
                    courseMap.put(curCourseID, curCourseName);
                }

                Subject newSubject = new Subject();
                newSubject.id = curID;
                newSubject.name = curContents.getString("name");
                newSubject.courses = courseMap;

                mSubjectsByCode.put(curID, newSubject);
            }

            // Set up subject names hashtable
            mSubjectsByName = new HashMap<String, String>();
            for(Iterator<String> namesIterator = names.keys(); namesIterator.hasNext();) {
                String curName = namesIterator.next();
                String curContents = names.getString(curName);
                mSubjectsByName.put(curName, curContents);
            }

            // Set up course names
            mCoursesByName = new HashMap<String, Course>();
            for(Iterator<String> coursesIterator = courses.keys(); coursesIterator.hasNext();) {
                String curCourseName = coursesIterator.next();
                JSONObject curContents = courses.getJSONObject(curCourseName);
                Course newCourse = new Course();
                newCourse.course = curContents.getString("course");
                newCourse.subj = curContents.getString("subj");
                mCoursesByName.put(curCourseName, newCourse);
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
                Subject curSubject = mSubjectsByCode.get(subjCode);
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
        Subject subject = mSubjectsByCode.get(subjectCode);
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
        Subject subject = mSubjectsByCode.get(subjectCode);
        if(subject == null) return null;
        if(!subject.courses.containsKey(courseCode)) return null;

        try {
            JSONObject newCourseJSON = new JSONObject();
            newCourseJSON.put("title", subject.courses.get(courseCode).toUpperCase());
            newCourseJSON.put("subjectCode", subjectCode);
            newCourseJSON.put("courseNumber", courseCode);
            newCourseJSON.put("stub", true);
            return newCourseJSON;
        } catch (JSONException e) {
            Log.w(TAG, "getCourseByCode(): " + e.getMessage());
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

        Set<Map.Entry<String, Course>> courseEntries = mCoursesByName.entrySet();
        for (Map.Entry<String, Course> curEntry: courseEntries) {
            // If there's a partial match on the full course name...
            if(StringUtils.containsIgnoreCase(curEntry.getKey(), query)) {
                Course curCourse = curEntry.getValue();
                String subjectCode = curCourse.subj;
                String courseCode = curCourse.course;

                try {
                    JSONObject newCourseJSON = new JSONObject();
                    newCourseJSON.put("title", curEntry.getKey().toUpperCase());
                    newCourseJSON.put("subjectCode", subjectCode);
                    newCourseJSON.put("courseNumber", courseCode);
                    newCourseJSON.put("stub", true);
                    results.add(newCourseJSON);
                    if(results.size() >= cap) return results;
                } catch (JSONException e) {
                    Log.w(TAG, "getCoursesByName(): " + e.getMessage());
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
