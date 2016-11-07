package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.RutgersAPI;
import edu.rutgers.css.Rutgers.model.SOCAPI;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Lists courses under a subject/department.
 */
public class SOCCourses extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCCourses";
    public static final String HANDLE               = "soccourses";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_CAMPUS_TAG      = "campus";
    private static final String ARG_SEMESTER_TAG    = "semester";
    private static final String ARG_LEVEL_TAG       = "level";
    private static final String ARG_SUBJECT_TAG     = "subject";

    /* Member data */
    private ScheduleAdapter mAdapter;
    private boolean mLoading;
    private String title;

    public static final class CourseData {
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

    public SOCCourses() {
        // Required empty public constructor
    }

    /** Create argument bundle for courses display */
    public static Bundle createArgs(@NonNull String title, @NonNull String campus, @NonNull String semester,
                                    @NonNull String level, @NonNull String subjectCode) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCCourses.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_CAMPUS_TAG, campus);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putString(ARG_LEVEL_TAG, level);
        bundle.putString(ARG_SUBJECT_TAG, subjectCode);
        return bundle;
    }

    /** Create argument bundle for courses display */
    public static Bundle createArgs(@NonNull String campus, @NonNull String semester,
                                    @NonNull String level, @NonNull String subjectCode) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCCourses.HANDLE);
        bundle.putString(ARG_CAMPUS_TAG, campus);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putString(ARG_LEVEL_TAG, level);
        bundle.putString(ARG_SUBJECT_TAG, subjectCode);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();

        title = WordUtils.capitalizeFully(args.getString(ARG_TITLE_TAG));
        if (title != null) {
            getActivity().setTitle(title);
        }

        final String campus = args.getString(ARG_CAMPUS_TAG);
        final String level = args.getString(ARG_LEVEL_TAG);
        final String semester = args.getString(ARG_SEMESTER_TAG);
        final String subjectCode = args.getString(ARG_SUBJECT_TAG);

        mAdapter = new ScheduleAdapter(new ArrayList<>(), R.layout.row_course);
        mAdapter.getPositionClicks()
            .map(course ->
                SOCSections.createArgs(course.getDisplayTitle(), campus,  semester, course)
            )
            .subscribe(this::switchFragments, this::logError);

        // Start loading courses
        mLoading = true;
        RutgersAPI.service.getSOCIndex(semester, campus, level)
            .flatMap(index -> SOCAPI.service.getCourses(semester, campus, level, subjectCode)
                .map(courses -> new CourseData(index.getSubjectByCode(subjectCode), courses))
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(courseData -> {
                reset();

                mAdapter.addAll(courseData.getCourses());
                title = WordUtils.capitalizeFully(courseData.getSubject().getDisplayTitle());
                getActivity().setTitle(title);
            }, this::logError);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);

        if (mLoading) showProgressCircle();

        if (title != null) {
            getActivity().setTitle(title);
        }

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public Link getLink() {
        final Bundle args = getArguments();
        final List<String> linkArgs = new ArrayList<>();
        linkArgs.add(args.getString(ARG_CAMPUS_TAG));
        linkArgs.add(args.getString(ARG_LEVEL_TAG));
        linkArgs.add(args.getString(ARG_SEMESTER_TAG));
        linkArgs.add(args.getString(ARG_SUBJECT_TAG));
        return new Link("soc", linkArgs, getLinkTitle());
    }

    private void reset() {
        mLoading = false;
        hideProgressCircle();
        mAdapter.clear();
    }
}