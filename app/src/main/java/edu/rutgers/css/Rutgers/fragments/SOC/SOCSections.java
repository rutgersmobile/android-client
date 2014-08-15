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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.CourseSectionAdapter;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers2.R;

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
                JSONObject clickedJSON = (JSONObject) parent.getItemAtPosition(position);

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