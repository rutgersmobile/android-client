package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.loader.CoursesLoader;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Lists courses under a subject/department.
 */
public class SOCCourses extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<List<Course>> {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCCourses";
    public static final String HANDLE               = "soccourses";

    private static final int LOADER_ID              = 1;

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_CAMPUS_TAG      = "campus";
    private static final String ARG_SEMESTER_TAG    = "semester";
    private static final String ARG_LEVEL_TAG       = "level";
    private static final String ARG_SUBJECT_TAG     = "subject";

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = "filter";

    /* Member data */
    private ScheduleAdapter mAdapter;
    private EditText mFilterEditText;
    private String mFilterString;
    private boolean mLoading;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, R.layout.row_section_header);

        // Restore filter
        if (savedInstanceState != null && savedInstanceState.getString(SAVED_FILTER_TAG) != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG);
        }

        // Start loading courses
        mLoading = true;
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_stickylist_progress);

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(WordUtils.capitalizeFully(args.getString(ARG_TITLE_TAG)));

        final String semester = args.getString(ARG_SEMESTER_TAG);

        mFilterEditText = (EditText) v.findViewById(R.id.filterEditText);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course clickedCourse = (Course) parent.getItemAtPosition(position);

                Bundle newArgs = SOCSections.createArgs(clickedCourse.getDisplayTitle(), semester, clickedCourse);
                switchFragments(newArgs);
            }
        });

        // Search text listener
        mFilterEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set filter for list adapter
                mFilterString = s.toString().trim();
                mAdapter.getFilter().filter(mFilterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Search clear button listener
        final ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);
        filterClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterEditText.setText(null);
            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilterEditText != null) outState.putString(SAVED_FILTER_TAG, mFilterString);
    }

    @Override
    public Loader<List<Course>> onCreateLoader(int id, Bundle args) {
        final String campus = args.getString(ARG_CAMPUS_TAG);
        final String level = args.getString(ARG_LEVEL_TAG);
        final String semester = args.getString(ARG_SEMESTER_TAG);
        final String subjectCode = args.getString(ARG_SUBJECT_TAG);

        return new CoursesLoader(getContext(), campus, level, semester, subjectCode);
    }

    @Override
    public void onLoadFinished(Loader<List<Course>> loader, List<Course> data) {
        // If response is empty assume that there was an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
        mLoading = false;
        hideProgressCircle();
        mAdapter.clear();
        mAdapter.addAllCourses(data);

        // Re-apply filter
        if (mFilterString != null && !mFilterString.isEmpty()) mAdapter.getFilter().filter(mFilterString);
    }

    @Override
    public void onLoaderReset(Loader<List<Course>> loader) {
        mLoading = false;
        hideProgressCircle();
        mAdapter.clear();
    }
}