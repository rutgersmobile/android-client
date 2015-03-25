package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.apache.commons.lang3.text.WordUtils;
import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

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
    private static final String ARG_SUBCODE_TAG     = "subjectCode";

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
        bundle.putString(ARG_SUBCODE_TAG, subjectCode);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        List<ScheduleAdapterItem> data = new ArrayList<>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, data);

        // Restore filter
        if (savedInstanceState != null && savedInstanceState.getString(SAVED_FILTER_TAG) != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG);
        }

        final String campus = args.getString(ARG_CAMPUS_TAG);
        final String level = args.getString(ARG_LEVEL_TAG);
        final String semester = args.getString(ARG_SEMESTER_TAG);
        final String subjectCode = args.getString(ARG_SUBCODE_TAG);

        mLoading = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(ScheduleAPI.getCourses(campus, level, semester, subjectCode)).done(new DoneCallback<List<Course>>() {

            @Override
            public void onDone(List<Course> result) {
                mAdapter.addAll(result);

                // Re-apply filter
                if (mFilterString != null && !mFilterString.isEmpty()) mAdapter.getFilter().filter(mFilterString);
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }

        }).always(new AlwaysCallback<List<Course>, Exception>() {
            @Override
            public void onAlways(Promise.State state, List<Course> resolved, Exception rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_list_progress);

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(WordUtils.capitalizeFully(args.getString(ARG_TITLE_TAG)));

        final String semester = args.getString(ARG_SEMESTER_TAG);

        mFilterEditText = (EditText) v.findViewById(R.id.filterEditText);

        final ListView listView = (ListView) v.findViewById(R.id.list);
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

}