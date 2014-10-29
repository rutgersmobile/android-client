package edu.rutgers.css.Rutgers.items.Schedule;

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
 * SOC Index.
 */
public class SOCIndex {

    private static final String TAG = "SOCIndex";

    private static class IndexCourse {
        String course;
        String subj;
    }

    private static class IndexSubject {
        String id;
        String name;
        HashMap<String, String> courses;
    }

    private String semesterCode;
    private String campusCode;
    private String levelCode;

    private HashMap<String, String[]> mAbbreviations;
    private HashMap<String, IndexCourse> mCoursesByName;
    private HashMap<String, IndexSubject> mSubjectsByCode;
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
            mSubjectsByCode = new HashMap<String, IndexSubject>();
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

                IndexSubject newSubject = new IndexSubject();
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
            mCoursesByName = new HashMap<String, IndexCourse>();
            for(Iterator<String> coursesIterator = courses.keys(); coursesIterator.hasNext();) {
                String curCourseName = coursesIterator.next();
                JSONObject curContents = courses.getJSONObject(curCourseName);
                IndexCourse newCourse = new IndexCourse();
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
    public List<Subject> getSubjectsByAbbreviation(String abbrev) {
        List<Subject> results = new ArrayList<Subject>();
        if(mAbbreviations.containsKey(abbrev)) {
            String[] subjCodes = mAbbreviations.get(abbrev);
            for(String subjCode: subjCodes) {
                IndexSubject curSubject = mSubjectsByCode.get(subjCode);
                if(curSubject != null) {
                    results.add(new Subject(curSubject.name, curSubject.id));
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
    public Subject getSubjectByCode(String subjectCode) {
        IndexSubject subject = mSubjectsByCode.get(subjectCode);
        if(subject != null) {
            return new Subject(subject.name, subject.id);
        } else {
            return null;
        }
    }

    /**
     * Get course by subject & course code combination
     * @param subjectCode Subject code
     * @param courseCode Course code
     * @return Course-stub JSON object
     */
    public Course getCourseByCode(String subjectCode, String courseCode) {
        IndexSubject subject = mSubjectsByCode.get(subjectCode);
        if(subject == null) return null;

        String title = subject.courses.get(courseCode);
        if(title != null) {
            return new Course(title.toUpperCase(), subjectCode, courseCode);
        } else {
            return null;
        }
    }

    /**
     * Get courses by partial title matches on query.
     * @param query Query string
     * @param cap Maximum number of results (cutoff point)
     * @return List of course-stub JSON objects (empty if no results found)
     */
    public List<Course> getCoursesByName(String query, int cap) {
        List<Course> results = new ArrayList<Course>();

        Set<Map.Entry<String, IndexCourse>> courseEntries = mCoursesByName.entrySet();
        for (Map.Entry<String, IndexCourse> curEntry: courseEntries) {
            // If there's a partial match on the full course name...
            if(StringUtils.containsIgnoreCase(curEntry.getKey(), query)) {
                IndexCourse curCourse = curEntry.getValue();
                String subjectCode = curCourse.subj;
                String courseCode = curCourse.course;

                results.add(new Course(curEntry.getKey().toUpperCase(), subjectCode, courseCode));
                if(results.size() >= cap) return results;
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
