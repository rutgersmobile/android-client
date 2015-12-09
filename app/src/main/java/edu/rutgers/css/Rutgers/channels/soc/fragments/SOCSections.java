package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleText;
import edu.rutgers.css.Rutgers.channels.soc.model.Section;
import edu.rutgers.css.Rutgers.channels.soc.model.SectionAdapterItem;
import edu.rutgers.css.Rutgers.channels.soc.model.loader.CourseLoader;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Display description and section information for a course.
 */
public class SOCSections extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<Course> {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCSections";
    public static final String HANDLE               = "socsections";

    private static final int LOADER_ID              = 1;

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_DATA_TAG        = "soc.sections.data";
    private static final String ARG_SEMESTER_TAG    = "semester";
    private static final String ARG_CAMPUS_CODE_TAG = "campusCode";
    private static final String ARG_SUBJECT_TAG     = "subject";
    private static final String ARG_COURSE_NUM_TAG  = "courseNumber";

    /* Member data */
    private CourseSectionAdapter mAdapter;
    private Course mCourse;

    private boolean mLoading;

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
        bundle.putString(ARG_CAMPUS_CODE_TAG, campusCode);
        bundle.putString(ARG_SUBJECT_TAG, subject);
        bundle.putString(ARG_COURSE_NUM_TAG, courseNumber);
        return bundle;
    }

    /** Create argument bundle for course sections display. */
    public static Bundle createArgs(@NonNull final String title, @NonNull final String semester, @NonNull final Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCSections.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putSerializable(ARG_DATA_TAG, course);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, new ArrayList<SectionAdapterItem>());

        Course course = (Course) args.getSerializable(ARG_DATA_TAG);
        if (course != null) {
            loadCourse(course);
            return;
        }

        mLoading = true;
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_list_progress, parent, false);
        final Bundle args = getArguments();

        if (mLoading) showProgressCircle();

        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));

        final String semester = args.getString(ARG_SEMESTER_TAG);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SectionAdapterItem clickedItem = (SectionAdapterItem) parent.getAdapter().getItem(position);

                if (clickedItem instanceof ScheduleText) {
                    ScheduleText scheduleText = (ScheduleText) clickedItem;

                    if (scheduleText.getType().equals(ScheduleText.TextType.PREREQS)) {
                        Bundle newArgs = TextDisplay.createArgs(mCourse.getSubject() + ":" + mCourse.getCourseNumber() + " Prerequisites", mCourse.getPreReqNotes());
                        switchFragments(newArgs);
                        return;
                    }
                } else if (clickedItem instanceof Section) {
                    Section section = (Section) clickedItem;

                    if (StringUtils.isNotBlank(section.getIndex()) && semester != null) {
                        String index = StringUtils.trim(section.getIndex());
                        switchFragments(WebDisplay.createArgs("WebReg", ScheduleAPI.getRegistrationLink(semester, index)));
                    } else {
                        Toast.makeText(getActivity(), R.string.soc_error_index, Toast.LENGTH_SHORT).show();
                        LOGW(TAG, "Section had no index field. Failed to launch webreg.");
                    }
                }
            }
        });

        return v;
    }

    private void loadCourse(@NonNull Course course) {
        mCourse = course;
        mAdapter.clear();

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
    public Loader<Course> onCreateLoader(int id, Bundle args) {
        return new CourseLoader(getContext(),
                args.getString(ARG_CAMPUS_CODE_TAG),
                args.getString(ARG_SEMESTER_TAG),
                args.getString(ARG_SUBJECT_TAG),
                args.getString(ARG_COURSE_NUM_TAG)
        );
    }

    @Override
    public void onLoadFinished(Loader<Course> loader, Course data) {
        if (data == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }
        mLoading = false;
        hideProgressCircle();
        loadCourse(data);
    }

    @Override
    public void onLoaderReset(Loader<Course> loader) {
        mLoading = false;
        hideProgressCircle();
        mAdapter.clear();
    }
}