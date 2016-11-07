package edu.rutgers.css.Rutgers.api.model.soc;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SOC Index.
 */
public class SOCIndex {

    private static final String TAG = "SOCIndex";

    public static class IndexCourse {
        private String course;
        private String subj;

        public IndexCourse() {}

        public IndexCourse(final String course, final String subj) {
            this.course = course;
            this.subj = subj;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public String getSubj() {
            return subj;
        }

        public void setSubj(String subj) {
            this.subj = subj;
        }
    }

    public static class IndexSubject {
        private String id;
        private String name;
        private HashMap<String, String> courses;

        public IndexSubject() {}

        public IndexSubject(final String id, final String name, final HashMap<String, String> courses) {
            this.id = id;
            this.name = name;
            this.courses = courses;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public HashMap<String, String> getCourses() {
            return courses;
        }

        public void setCourses(HashMap<String, String> courses) {
            this.courses = courses;
        }
    }

    private String semesterCode;

    public String getSemesterCode() {
        return semesterCode;
    }

    public void setSemesterCode(String semesterCode) {
        this.semesterCode = semesterCode;
    }

    private String campusCode;

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    private String levelCode;

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    private HashMap<String, String[]> mAbbreviations;
    private HashMap<String, IndexCourse> mCoursesByName;
    private HashMap<String, IndexSubject> mSubjectsByCode;
    private HashMap<String, String> mSubjectsByName;

    public SOCIndex(HashMap<String, String[]> abbreviations, HashMap<String, IndexCourse> coursesByName,
                    HashMap<String, IndexSubject> subjectsByCode, HashMap<String, String> subjectsByName) {

        mAbbreviations = abbreviations;
        mCoursesByName = coursesByName;
        mSubjectsByCode = subjectsByCode;
        mSubjectsByName = subjectsByName;
    }

    public List<Subject> getSubjects() {
        List<Subject> results = new ArrayList<>();
        for (IndexSubject subject : mSubjectsByCode.values()) {
            results.add(new Subject(subject.name.toUpperCase(), subject.id));
        }
        return results;
    }

    /**
     * Get subjects by abbreviation
     * @param abbrev Abbreviation
     * @return List of subject JSON objects (empty if no results found)
     */
    public List<Subject> getSubjectsByAbbreviation(String abbrev) {
        List<Subject> results = new ArrayList<>();
        if (mAbbreviations.containsKey(abbrev.toUpperCase())) {
            String[] subjCodes = mAbbreviations.get(abbrev.toUpperCase());
            for (String subjCode: subjCodes) {
                IndexSubject curSubject = mSubjectsByCode.get(subjCode);
                if (curSubject != null) {
                    results.add(new Subject(curSubject.name.toUpperCase(), curSubject.id));
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
        if (subject != null) {
            return new Subject(subject.name.toUpperCase(), subject.id);
        } else {
            return null;
        }
    }

    public List<Course> getCoursesInSubject(String subjectCode) {
        List<Course> results = new ArrayList<>();
        IndexSubject subject = mSubjectsByCode.get(subjectCode);
        if (subject != null) {
            Set<Map.Entry<String, String>> courseEntries = subject.courses.entrySet();
            for (Map.Entry<String, String> courseEntry : courseEntries) {
                String courseCode = courseEntry.getKey();
                String courseTitle = courseEntry.getValue();
                results.add(new Course(courseTitle, subjectCode, courseCode));
            }
        }
        Collections.sort(results);
        return results;
    }

    public List<Course> getCoursesByCodes(String queryCourseCode, List<String> query) {
        List<Course> results = new ArrayList<>();
        Set<Map.Entry<String, IndexCourse>> courseEntries = mCoursesByName.entrySet();
        for (Map.Entry<String, IndexCourse> courseEntry : courseEntries) {
            String courseName = courseEntry.getKey();
            String courseCode = courseEntry.getValue().course;
            String subjectCode = courseEntry.getValue().subj;

            if (query.isEmpty() || allContained(query, courseName)) {
                if (queryCourseCode == null || courseCode.contains(queryCourseCode)) {
                    results.add(new Course(courseName, subjectCode, courseCode));
                }
            }
        }
        Collections.sort(results);
        return results;
    }

    public Course getCourseByCode(final String sCode, final String cCode) {
        for (Map.Entry<String, IndexCourse> e : mCoursesByName.entrySet()) {
            final String courseName = e.getKey();
            final String courseCode = e.getValue().course;
            final String subjectCode = e.getValue().subj;

            if (courseCode.equals(cCode) && subjectCode.equals(sCode)) {
                return new Course(courseName, subjectCode, courseCode);
            }
        }

        return null;
    }

    public List<Course> getCoursesByCodeInSubjects(Collection<Subject> subjects, String courseCodeQuery) {
        List<Course> results = new ArrayList<>();

        for (Subject subject : subjects) {
            String subjectCode = subject.getCode();
            IndexSubject indexSubject = mSubjectsByCode.get(subjectCode);
            if (indexSubject == null) continue;

            List<Course> sortList = new ArrayList<>();
            for (Map.Entry<String, String> courseEntry : indexSubject.courses.entrySet()) {
                String courseCode = courseEntry.getKey();
                String courseTitle = courseEntry.getValue();

                if (courseCode.startsWith(courseCodeQuery)) {
                    sortList.add(new Course(courseTitle, subjectCode, courseCode));
                }
            }
            Collections.sort(sortList);
            results.addAll(sortList);
        }

        return results;
    }

    /**
     * Get courses by partial title matches on query in a subject
     * @param subjects subjects to look for courses in
     * @param query Query string
     * @param cap Maximum number of results (cutoff point)
     * @return List of course-stub JSON objects (empty if no results found)
     */
    public List<Course> getCoursesByNameInSubjects(Collection<Subject> subjects, List<String> query, int cap) {
        List<Course> results = new ArrayList<>();
        boolean hitCap = false;

        for (Subject subject : subjects) {
            String subjectCode = subject.getCode();
            IndexSubject indexSubject = mSubjectsByCode.get(subjectCode);
            if (indexSubject == null) continue;

            List<Course> sortList = new ArrayList<>();
            for (Map.Entry<String, String> courseEntry : indexSubject.courses.entrySet()) {
                String courseCode = courseEntry.getKey();
                String courseTitle = courseEntry.getValue();

                if (allContained(query, courseTitle)) {
                    sortList.add(new Course(courseTitle, subjectCode, courseCode));
                }
                hitCap = results.size() + sortList.size() >= cap;
                if (hitCap) break;
            }
            Collections.sort(sortList);
            results.addAll(sortList);
            if (hitCap) return results;
        }

        return results;
    }

    private boolean allContained(List<String> words, String s) {
        for (String word : words) {
            if (!StringUtils.containsIgnoreCase(s, word)) {
                return false;
            }
        }
        return true;
    }
}
