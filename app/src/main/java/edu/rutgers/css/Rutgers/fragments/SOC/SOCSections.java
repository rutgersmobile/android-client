package edu.rutgers.css.Rutgers.fragments.SOC;

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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.items.Schedule.Course;
import edu.rutgers.css.Rutgers.items.Schedule.ScheduleText;
import edu.rutgers.css.Rutgers.items.Schedule.Section;
import edu.rutgers.css.Rutgers.items.Schedule.SectionAdapterItem;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Display description and section information for a course.
 */
public class SOCSections extends Fragment {

    private static final String TAG = "SOCSections";
    public static final String HANDLE = "socsections";

    private List<SectionAdapterItem> mData;
    private CourseSectionAdapter mAdapter;

    public SOCSections() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mData = new ArrayList<SectionAdapterItem>();
        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, mData);

        Gson gson = new Gson();
        if(args.getString("data") == null) {
            Log.e(TAG, "Course data not set");
            AppUtil.showFailedLoadToast(getActivity());
            return;
        }

        // TODO Replace with just getting course from API
        Course course = gson.fromJson(args.getString("data"), Course.class);

        // Add course description if set
        if(StringUtils.isNotBlank(course.getCourseDescription())) {
            mAdapter.add(new ScheduleText(course.getCourseDescription(), ScheduleText.TextType.DESCRIPTION));
        }

        /*
        // Add synopsis link if set
        if(StringUtils.isNotBlank(course.getSynopsisUrl())) {
            mAdapter.add(new ScheduleText("Synopsis", ScheduleText.TextType.SYNOPSIS));
        }
        */

        // Add prerequisites button if set
        if(StringUtils.isNotBlank(course.getPreReqNotes())) {
            mAdapter.add(new ScheduleText("Prerequisites", ScheduleText.TextType.PREREQS));
        }

        // Add all visible sections to list
        for(Section section: course.getSections()) {
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

                    // TODO implement this again
                    return;
                } else if(clickedItem instanceof Section) {
                    Section section = (Section) clickedItem;

                    if(StringUtils.isNotBlank(section.getIndex()) && semester != null) {
                        String index = StringUtils.trim(section.getIndex());
                        Schedule.openRegistrationWindow(semester, index);
                    }
                }

                // It's a section row, open WebReg for this section
                if (!clickedJSON.isNull("index") && semester != null) {
                    String index = clickedJSON.optString("index");
                    Schedule.openRegistrationWindow(semester, index);
                } else {
                    Toast.makeText(getActivity(), R.string.soc_error_index, Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Section had no index field. Failed to launch webreg.");
                }

                /* OLD - don't uncomment
                // Check if it's a synopsis row; send to synopsis URL
                if(clickedJSON.optBoolean("isSynopsisRow")) {
                    Bundle args = new Bundle();
                    args.putString("component", WebDisplay.HANDLE);
                    args.putString("url", clickedJSON.optString("synopsisUrl"));
                    ComponentFactory.getInstance().switchFragments(args);
                    return;
                }

                // Check if it's a pre-req row, open pre-reqs if so
                else if(clickedJSON.optBoolean("isPreReqRow")) {
                    Bundle args = new Bundle();
                    args.putString("component", TextDisplay.HANDLE);
                    args.putString("data", clickedJSON.optString("preReqNotes"));
                    ComponentFactory.getInstance().switchFragments(args);
                    return;
                }

                // Check if it's a description row; do nothing
                else if(clickedJSON.optBoolean("isDescriptionRow")) {
                    return;
                }
                */
            }
        });

        return v;
    }
    
}