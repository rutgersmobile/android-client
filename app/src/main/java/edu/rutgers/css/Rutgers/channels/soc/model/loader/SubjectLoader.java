package edu.rutgers.css.Rutgers.channels.soc.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.api.soc.ScheduleAPI;
import edu.rutgers.css.Rutgers.api.soc.model.Semesters;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 *
 */
public class SubjectLoader extends SimpleAsyncLoader<SubjectLoader.SubjectHolder> {
    public static final String TAG = "SubjectLoader";

    String semester;
    String level;
    String campus;

    public class SubjectHolder {
        private final SOCIndex index;
        private final List<Subject> subjects;
        private final Semesters semesters;
        private final String semester;
        private final String defaultSemester;

        public SubjectHolder(final SOCIndex index, final List<Subject> subjects, final Semesters semesters,
                             final String semester, final String defaultSemester) {
            this.index = index;
            this.subjects = subjects;
            this.semesters = semesters;
            this.semester = semester;
            this.defaultSemester = defaultSemester;
        }

        public SOCIndex getIndex() {
            return index;
        }

        public List<Subject> getSubjects() {
            return subjects;
        }

        public Semesters getSemesters() {
            return semesters;
        }

        public String getSemester() {
            return semester;
        }

        public String getDefaultSemester() {
            return defaultSemester;
        }
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
            // Get semesters to make sure the semester we're looking at makes sense
            result = ScheduleAPI.getSemesters();
            int defaultIndex = result.getDefaultSemester();
            List<String> semesters = result.getSemesters();

            if (semesters.isEmpty()) {
                LOGE(TAG, "Semesters list is empty");
                return null;
            } else if (defaultIndex < 0 || defaultIndex >= semesters.size()) {
                // index should be within the size of the array it is indexing into
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

            // Now that we have the correct semester we can get
            // the index for searching and subjects to show on the main page
            socIndex = ScheduleAPI.getIndex(semester, campus, level);
            subjects = ScheduleAPI.getSubjects(campus, level, semester);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return new SubjectHolder(socIndex, subjects, result, semester, defaultSemester);
    }
}
