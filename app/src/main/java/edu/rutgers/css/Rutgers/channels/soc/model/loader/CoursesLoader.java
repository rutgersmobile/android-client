package edu.rutgers.css.Rutgers.channels.soc.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Loader for getting multiple courses from the SoC API
 */
public class CoursesLoader extends SimpleAsyncLoader<List<Course>> {
    String campus;
    String level;
    String semester;
    String subjectCode;

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
    public List<Course> loadInBackground() {
        try {
            return ScheduleAPI.getCourses(campus, level, semester, subjectCode);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }
}
