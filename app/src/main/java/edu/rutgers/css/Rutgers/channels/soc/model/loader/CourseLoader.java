package edu.rutgers.css.Rutgers.channels.soc.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.model.soc.Course;
import edu.rutgers.css.Rutgers.api.model.soc.ScheduleAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Loader for SoC courses
 */
public class CourseLoader extends SimpleAsyncLoader<Course> {
    String campusCode;
    String semesterCode;
    String subjectCode;
    String courseCode;

    private static final String TAG = "CourseLoader";

    public CourseLoader(Context context, String campusCode, String semesterCode, String subjectCode, String courseCode) {
        super(context);
        this.campusCode = campusCode;
        this.semesterCode = semesterCode;
        this.subjectCode = subjectCode;
        this.courseCode = courseCode;
    }

    @Override
    public Course loadInBackground() {
        Course course = null;
        try {
            course = ScheduleAPI.getCourse(campusCode, semesterCode, subjectCode, courseCode);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }
        return course;
    }
}
