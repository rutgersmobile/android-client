package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.Schedule;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleText;
import edu.rutgers.css.Rutgers.channels.soc.model.Section;
import edu.rutgers.css.Rutgers.channels.soc.model.SectionAdapterItem;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * Display description and section information for a course.
 */
public class SOCSections extends Fragment {

    private static final String TAG = "SOCSections";
    public static final String HANDLE = "socsections";

    private CourseSectionAdapter mAdapter;
    private Course mCourse;

    public SOCSections() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        List<SectionAdapterItem> data = new ArrayList<SectionAdapterItem>();
        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, data);

        Gson gson = new Gson();
        if(args.getString("data") == null) {
            Log.e(TAG, "Course data not set");
            AppUtils.showFailedLoadToast(getActivity());
            return;
        }

        // TODO Replace with just getting course from API
        try {
            mCourse = gson.fromJson(args.getString("data"), Course.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        // Add course description if set
        if(StringUtils.isNotBlank(mCourse.getCourseDescription())) {
            mAdapter.add(new ScheduleText(mCourse.getCourseDescription(), ScheduleText.TextType.DESCRIPTION));
        }

        // Add prerequisites button if set
        if(StringUtils.isNotBlank(mCourse.getPreReqNotes())) {
            mAdapter.add(new ScheduleText("Prerequisites", ScheduleText.TextType.PREREQS));
        }

        // Add all visible sections to list
        for(Section section: mCourse.getSections()) {
            if("Y".equalsIgnoreCase(section.getPrinted())) mAdapter.add(section);
        }

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_soc_sections, parent, false);
        Bundle args = getArguments();

        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));

        final String semester = args.getString("semester");

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SectionAdapterItem clickedItem = (SectionAdapterItem) parent.getAdapter().getItem(position);

                if(clickedItem instanceof ScheduleText) {
                    ScheduleText scheduleText = (ScheduleText) clickedItem;

                    if(scheduleText.getType().equals(ScheduleText.TextType.PREREQS)) {
                        Bundle newArgs = new Bundle();
                        newArgs.putString("component", TextDisplay.HANDLE);
                        newArgs.putString("title", mCourse.getSubject()+":"+mCourse.getCourseNumber()+" Prerequisites");
                        newArgs.putString("data", mCourse.getPreReqNotes());
                        ComponentFactory.getInstance().switchFragments(newArgs);
                        return;
                    }
                } else if(clickedItem instanceof Section) {
                    Section section = (Section) clickedItem;

                    if(StringUtils.isNotBlank(section.getIndex()) && semester != null) {
                        String index = StringUtils.trim(section.getIndex());
                        Schedule.openRegistrationWindow(semester, index);
                    } else {
                        Toast.makeText(getActivity(), R.string.soc_error_index, Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Section had no index field. Failed to launch webreg.");
                    }
                }
            }
        });

        return v;
    }
    
}