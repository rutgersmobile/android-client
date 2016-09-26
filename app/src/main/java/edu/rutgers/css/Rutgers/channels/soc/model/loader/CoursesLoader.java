package edu.rutgers.css.Rutgers.channels.soc.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.List;

import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.api.soc.ScheduleAPI;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Loader for getting multiple courses from the SoC API
 */
public class CoursesLoader extends SimpleAsyncLoader<CoursesLoader.CourseData> {
    String campus;
    String level;
    String semester;
    String subjectCode;

    public final class CourseData {
        private final Subject subject;
        private final List<Course> courses;

        public CourseData(final Subject subject, final List<Course> courses) {
            this.subject = subject;
            this.courses = courses;
        }

        public Subject getSubject() {
            return subject;
        }

        public List<Course> getCourses() {
            return courses;
        }
    }

    public static final String TAG = "CoursesLoader";

    /**
     * @param context Application context
     * @param campus NB, NWK, etc. for the campus that the class is on
     * @param level UG or G for undergraduate and graduate respectively
     * @param semester Typically a month number prepended to a year: 92015
     * @param subjectCode The numerical representation of a subject: 198 -> Computer Science
     */
    public CoursesLoader(Context context, String campus, String level, String semester, String subjectCode) {
        super(context);
        this.campus = campus;
        this.level = level;
        this.semester = semester;
        this.subjectCode = subjectCode;
    }

    @Override
    public CourseData loadInBackground() {
        try {
            SOCIndex index = ScheduleAPI.getIndex(semester, campus, level);
            Subject subject = index.getSubjectByCode(subjectCode);
            List<Course> courses = ScheduleAPI.getCourses(campus, level, semester, subjectCode);
            return new CourseData(subject, courses);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }
}
