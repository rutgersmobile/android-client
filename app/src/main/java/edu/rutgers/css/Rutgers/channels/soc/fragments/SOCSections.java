package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleText;
import edu.rutgers.css.Rutgers.channels.soc.model.Section;
import edu.rutgers.css.Rutgers.channels.soc.model.SectionAdapterItem;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

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

    /* Member data */
    private CourseSectionAdapter mAdapter;
    private Course mCourse;

    public SOCSections() {
        // Required empty public constructor
    }

    /** Create argument bundle for course sections display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String semester, @NonNull Course course) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCSections.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_SEMESTER_TAG, semester);
        bundle.putString(ARG_DATA_TAG, new Gson().toJson(course));
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        Gson gson = new Gson();
        List<SectionAdapterItem> data = new ArrayList<>();
        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, data);

        if (args.getString(ARG_DATA_TAG) == null) {
            LOGE(TAG, "Course data not set");
            AppUtils.showFailedLoadToast(getActivity());
            return;
        }

        // TODO Replace with just getting course from API
        try {
            mCourse = gson.fromJson(args.getString(ARG_DATA_TAG), Course.class);
        } catch (JsonSyntaxException e) {
            LOGE(TAG, e.getMessage());
            return;
        }

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
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_list_progress, parent, false);
        final Bundle args = getArguments();

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
                        Bundle newArgs = TextDisplay.createArgs(mCourse.getSubject()+":"+mCourse.getCourseNumber()+" Prerequisites", mCourse.getPreReqNotes());
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
    
}