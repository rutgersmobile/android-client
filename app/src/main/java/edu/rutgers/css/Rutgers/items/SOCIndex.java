package edu.rutgers.css.Rutgers.items;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    private HashMap<String, String[]> mAbbrevations;
    private HashMap<String, Course> mCourses;
    private HashMap<String, Subject> mSubjects;
    private HashMap<String, String> mSubjectNames;
    /*private MutableBkTree<String> mCourseBKTree;*/

    public SOCIndex(JSONObject index) {
        if(!(index.has("abbrevs") && index.has("courses") && index.has("ids") && index.has("names"))) {
            throw new IllegalArgumentException("Invalid index, missing critical fields");
        }

        // Convert the JSON into native hashtables
        try {
            JSONObject abbrevs = index.getJSONObject("abbrevs"); // List of subject abbrevs->sub IDs
            JSONObject ids = index.getJSONObject("ids"); // List of subject IDs->contained courses
            JSONObject names = index.getJSONObject("names"); // List of subject names->sub IDs
            JSONObject courses = index.getJSONObject("courses"); // List of course names->sub/course IDs

            // Set up abbreviations hashtable
            mAbbrevations = new HashMap<String, String[]>();
            Iterator<String> abbrevsIterator = abbrevs.keys();
            while(abbrevsIterator.hasNext()) {
                String curAbbrev = abbrevsIterator.next();
                JSONArray curContents = abbrevs.getJSONArray(curAbbrev);
                String[] subIDStrings = JsonUtil.jsonToStringArray(curContents);
                mAbbrevations.put(curAbbrev, subIDStrings);
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
            /*mCourseBKTree = new MutableBkTree<String>(this);*/
            Iterator<String> coursesIterator = courses.keys();
            while(coursesIterator.hasNext()) {
                String curCourseName = coursesIterator.next();
                JSONObject curContents = courses.getJSONObject(curCourseName);
                Course newCourse = new Course();
                newCourse.course = curContents.getString("course");
                newCourse.subj = curContents.getString("subj");
                mCourses.put(curCourseName, newCourse);
                /*mCourseBKTree.add(curCourseName);*/
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid index JSON: " + e.getMessage());
        }
    }

/*    // Use Levenshtein distance for BK tree
    @Override
    public int distance(String x, String y) {
        return StringUtils.getLevenshteinDistance(x.toLowerCase(),y.toLowerCase());
    }*/

    /**
     * Get subjects by abbreviation
     * @param abbrev Abbreviation
     * @return List of Subject JSON (empty if no results found)
     */
    public List<JSONObject> getSubjectsByAbbreviation(String abbrev) {
        List<JSONObject> results = new ArrayList<JSONObject>();
        if(mAbbrevations.containsKey(abbrev)) {
            String[] subjCodes = mAbbrevations.get(abbrev);
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

    public JSONObject getCourseByCode(String subjectCode, String courseCode) {
        Subject subject = mSubjects.get(subjectCode);
        if(subject == null) return null;
        if(!subject.courses.containsKey(courseCode)) return null;

        try {
            JSONObject newCourseJSON = new JSONObject();
            newCourseJSON.put("courseNumber", courseCode);
            newCourseJSON.put("title", subject.courses.get(courseCode).toUpperCase());
            // TODO Full course info so that sections and credits values aren't empty
            return newCourseJSON;
        } catch (JSONException e) {
            Log.w(TAG, "getCourseByCode(): " + e.getMessage());
            return null;
        }
    }

}
