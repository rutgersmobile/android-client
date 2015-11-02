package edu.rutgers.css.Rutgers.channels.soc.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
import edu.rutgers.css.Rutgers.channels.soc.model.Subject;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import lombok.Data;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 *
 */
public class SubjectLoader extends SimpleAsyncLoader<SubjectLoader.SubjectHolder> {
    public static final String TAG = "SubjectLoader";

    String semester;
    String level;
    String campus;

    @Data
    public class SubjectHolder {
        final SOCIndex index;
        final List<Subject> subjects;
        final Semesters semesters;
        final String semester;
        final String defaultSemester;
    }

    public SubjectLoader(Context context, String semester, String level, String campus) {
        super(context);
        this.semester = semester;
        this.level = level;
        this.campus = campus;
    }

    @Override
    public SubjectHolder loadInBackground() {
        SOCIndex socIndex = null;
        List<Subject> subjects = null;
        Semesters result = null;
        String defaultSemester = null;
        try {
            result = ScheduleAPI.getSemesters();
            int defaultIndex = result.getDefaultSemester();
            List<String> semesters = result.getSemesters();

            if (semesters.isEmpty()) {
                LOGE(TAG, "Semesters list is empty");
                return null;
            } else if (defaultIndex < 0 || defaultIndex >= semesters.size()) {
                LOGW(TAG, "Invalid default index " + defaultIndex);
                defaultIndex = 0;
            }

            defaultSemester = semesters.get(defaultIndex);

            // If there is a saved semester setting, make sure it's valid
            if (semester == null || !semesters.contains(semester)) {
                semester = defaultSemester;
            }

            if (BuildConfig.DEBUG) {
                for (String semester : semesters) {
                    LOGV(TAG, "Got semester: " + ScheduleAPI.translateSemester(semester));
                }
                LOGV(TAG, "Default semester: " + ScheduleAPI.translateSemester(defaultSemester));
            }

            socIndex = ScheduleAPI.getIndex(semester, campus, level);
            subjects = ScheduleAPI.getSubjects(campus, level, semester);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return new SubjectHolder(socIndex, subjects, result, semester, defaultSemester);
    }
}
