package edu.rutgers.css.Rutgers.fragments.SOC;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import edu.rutgers.css.Rutgers2.R;

/**
 * Display description and section information for a course.
 */
public class SOCSections extends Fragment {

    private static final String TAG = "SOCSections";
    public static final String HANDLE = "socsections";

    private List<JSONObject> mData;
    private CourseSectionAdapter mAdapter;

    public SOCSections() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mData = new ArrayList<JSONObject>();
        mAdapter = new CourseSectionAdapter(getActivity(), R.layout.row_course_section, mData);

        try {
            JSONObject courseJSON = new JSONObject(args.getString("data"));

            // Create a course description row, if a description is specified
            if(!courseJSON.isNull("courseDescription") && !courseJSON.getString("courseDescription").isEmpty()) {
                JSONObject descriptionRow = new JSONObject();
                descriptionRow.put("isDescriptionRow", true);
                descriptionRow.put("courseDescription", courseJSON.getString("courseDescription"));
                if(!courseJSON.isNull("synopsisUrl")) descriptionRow.put("synopsisUrl", courseJSON.optString("synopsisUrl"));
                mAdapter.add(descriptionRow);
            }

            // Create pre-req row if pre-reqs are specified
            if(!courseJSON.isNull("preReqNotes") && !courseJSON.getString("preReqNotes").isEmpty()) {
                JSONObject preReqRow = new JSONObject();
                preReqRow.put("isPreReqRow", true);
                preReqRow.put("preReqNotes", courseJSON.getString("preReqNotes"));
                mAdapter.add(preReqRow);
            }

            // Load the section rows
            JSONArray sections = courseJSON.getJSONArray("sections");
            for(int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                // Only add "visible" sections to list
                if(section.getString("printed").equalsIgnoreCase("Y")) mAdapter.add(section);
            }
        } catch (JSONException e) {
            Log.w(TAG, "onCreate(): " + e.getMessage());
            Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
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
                final JSONObject clickedJSON = (JSONObject) parent.getItemAtPosition(position);

                // Check if it's a description row; send to synopsis URL
                if(clickedJSON.optBoolean("isDescriptionRow")) {
                    if(!clickedJSON.isNull("synopsisUrl")) {
                        Bundle args = new Bundle();
                        args.putString("component", WebDisplay.HANDLE);
                        args.putString("url", clickedJSON.optString("synopsisUrl"));
                        ComponentFactory.getInstance().switchFragments(args);
                    }
                    return;
                }

                // Check if it's a pre-req row, open pre-reqs if so
                if(clickedJSON.optBoolean("isPreReqRow")) {
                    Bundle args = new Bundle();
                    args.putString("component", TextDisplay.HANDLE);
                    args.putString("data", clickedJSON.optString("preReqNotes"));
                    ComponentFactory.getInstance().switchFragments(args);
                    return;
                }

                // It's a section row, open WebReg for this section
                String index = clickedJSON.optString("index");
                if (index != null && semester != null) {
                    Schedule.openRegistrationWindow(semester, index);
                } else {
                    Toast.makeText(getActivity(), R.string.soc_error_index, Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Section had no index field. Failed to launch webreg.");
                }
            }
        });

        return v;
    }
    
}