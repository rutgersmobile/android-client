package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.gson.Gson;

import org.apache.commons.lang3.text.WordUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.Schedule;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Lists courses under a subject/department.
 */
public class SOCCourses extends Fragment {

    private static final String TAG = "SOCCourses";
    public static final String HANDLE = "soccourses";

    private ScheduleAdapter mAdapter;
    private EditText mFilterEditText;
    private String mFilterString;

    public SOCCourses() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        List<ScheduleAdapterItem> data = new ArrayList<ScheduleAdapterItem>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, data);

        // Restore filter
        if(savedInstanceState != null && savedInstanceState.getString("filter") != null) {
            mFilterString = savedInstanceState.getString("filter");
        }

        final String campus = args.getString("campus");
        final String level = args.getString("level");
        final String semester = args.getString("semester");
        final String subjectCode = args.getString("subjectCode");

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Schedule.getCourses(campus, level, semester, subjectCode)).done(new DoneCallback<List<Course>>() {

            @Override
            public void onDone(List<Course> result) {
                mAdapter.addAll(result);

                // Re-apply filter
                if(mFilterString != null && !mFilterString.isEmpty()) mAdapter.getFilter().filter(mFilterString);
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception result) {
                AppUtil.showFailedLoadToast(getActivity());
            }

        });
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_soc_main, parent, false);
        Bundle args = getArguments();

        if(args.getString("title") != null) getActivity().setTitle(WordUtils.capitalizeFully(args.getString("title")));

        final String semester = args.getString("semester");

        mFilterEditText = (EditText) v.findViewById(R.id.filterEditText);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course clickedCourse = (Course) parent.getItemAtPosition(position);

                Bundle newArgs = new Bundle();
                newArgs.putString("component", SOCSections.HANDLE);
                newArgs.putString("title", clickedCourse.getDisplayTitle());
                newArgs.putString("data", new Gson().toJson(clickedCourse));
                newArgs.putString("semester", semester);

                ComponentFactory.getInstance().switchFragments(newArgs);
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
        if(mFilterEditText != null) outState.putString("filter", mFilterString);
    }

}