package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.SOCAPI;
import edu.rutgers.css.Rutgers.api.model.soc.Course;
import edu.rutgers.css.Rutgers.api.model.soc.Section;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleText;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Display description and section information for a course.
 */
public class SOCSections extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCSections";
    public static final String HANDLE               = "socsections";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_DATA_TAG        = "soc.sections.data";
    private static final String ARG_SEMESTER_TAG    = "semester";
    private static final String ARG_CAMPUS_TAG      = "campus";
    private static final String ARG_SUBJECT_TAG     = "subject";
    private static final String ARG_COURSE_TAG      = "course";

    /* Member data */
    private CourseSectionAdapter mAdapter;
    private Course mCourse;
    private String semester;
    private String title;

    public SOCSections() {
        // Required empty public constructor
    }

    /** Create argument bundle for course sections display. */
    public static Bundle createArgs(@NonNull final String title, @NonNull final String semester,
                                    @NonNull final String campusCode,
                                    @NonNull final String subject, @NonNull final String courseNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCSections.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putString(ARG_CAMPUS_TAG, campusCode);
        bundle.putString(ARG_SUBJECT_TAG, subject);
        bundle.putString(ARG_COURSE_TAG, courseNumber);
        return bundle;
    }

    /** Create argument bundle for course sections display. */
    public static Bundle createArgs(@NonNull final String title, @NonNull final String campus, @NonNull final String semester, @NonNull final Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCSections.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_CAMPUS_TAG, campus);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putSerializable(ARG_DATA_TAG, course);
        return bundle;
    }

    /** Create argument bundle for course sections display. */
    public static Bundle createArgs(@NonNull final String semester, @NonNull final String campusCode,
                                    @NonNull final String subject, @NonNull final String courseNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCSections.HANDLE);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putString(ARG_CAMPUS_TAG, campusCode);
        bundle.putString(ARG_SUBJECT_TAG, subject);
        bundle.putString(ARG_COURSE_TAG, courseNumber);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();

        title = WordUtils.capitalizeFully(args.getString(ARG_TITLE_TAG));
        if (title != null) {
            getActivity().setTitle(title);
        }
        semester = args.getString(ARG_SEMESTER_TAG);

        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, new ArrayList<>());

        Course course = (Course) args.getSerializable(ARG_DATA_TAG);
        if (course != null) {
            loadCourse(course);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final Bundle args = getArguments();

        mAdapter.getPositionClicks()
            .flatMap(clickedItem -> {
                if (clickedItem instanceof ScheduleText) {
                    ScheduleText scheduleText = (ScheduleText) clickedItem;

                    if (scheduleText.getType().equals(ScheduleText.TextType.PREREQS)) {
                        return Observable.just(
                            TextDisplay.createArgs(mCourse.getSubject() + ":" + mCourse.getCourseNumber() + " Prerequisites", mCourse.getPreReqNotes())
                        );
                    }
                } else if (clickedItem instanceof Section) {
                    Section section = (Section) clickedItem;

                    if (StringUtils.isNotBlank(section.getIndex()) && semester != null) {
                        String index = StringUtils.trim(section.getIndex());
                        return Observable.just(
                            WebDisplay.createArgs("WebReg", SOCAPI.getRegistrationLink(semester, index))
                        );
                    } else {
                        Toast.makeText(getActivity(), R.string.soc_error_index, Toast.LENGTH_SHORT).show();
                        return Observable.error(
                            new IllegalStateException("Section had no index field. Failed to launch webreg.")
                        );
                    }
                }

                return null;
            })
            .compose(bindToLifecycle())
            .subscribe(this::switchFragments, this::logError);

        setLoading(true);
        final String campus = args.getString(ARG_CAMPUS_TAG);
        final String semester = args.getString(ARG_SEMESTER_TAG);
        final String subject = args.getString(ARG_SUBJECT_TAG);
        final String courseCode = args.getString(ARG_COURSE_TAG);
        SOCAPI.getCourse(semester, campus, subject, courseCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .retryWhen(this::logAndRetry)
            .subscribe(this::loadCourse, this::handleErrorWithRetry);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);

        if (title != null) getActivity().setTitle(title);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public Link getLink() {
        final Bundle args = getArguments();
        final List<String> argParts = new ArrayList<>();
        if (mCourse == null) {
            argParts.add(args.getString(ARG_CAMPUS_TAG));
            argParts.add(args.getString(ARG_SEMESTER_TAG));
            argParts.add(args.getString(ARG_SUBJECT_TAG));
            argParts.add(args.getString(ARG_COURSE_TAG));
        } else {
            argParts.add(args.getString(ARG_CAMPUS_TAG));
            argParts.add(args.getString(ARG_SEMESTER_TAG));
            argParts.add(mCourse.getSubject());
            argParts.add(mCourse.getCourseNumber());
        }

        return new Link("soc", argParts, getLinkTitle());
    }

    private void loadCourse(@NonNull Course course) {
        reset();
        mCourse = course;

        title = WordUtils.capitalizeFully(course.getTitle());
        getActivity().setTitle(title);

        // Add course description if set
        if (StringUtils.isNotBlank(mCourse.getCourseDescription())) {
            mAdapter.add(new ScheduleText(mCourse.getCourseDescription(), ScheduleText.TextType.DESCRIPTION));
        }

        // Add prerequisites button if set
        if (StringUtils.isNotBlank(mCourse.getPreReqNotes())) {
            mAdapter.add(new ScheduleText("Prerequisites", ScheduleText.TextType.PREREQS));
        }

        // Add all visible sections to list
        for (Section section: mCourse.getSections()) {
            if ("Y".equalsIgnoreCase(section.getPrinted())) mAdapter.add(section);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        mAdapter.clear();
    }
}