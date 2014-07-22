package edu.rutgers.css.Rutgers.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.AppUtil;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers2.R;

public class SOCCourses extends Fragment {

    private static final String TAG = "SOCDisplay";

    private ArrayList<RMenuPart> mData;
    private RMenuAdapter mAdapter;
    private ListView mListView;

    public SOCCourses() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mData = new ArrayList<RMenuPart>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.title_row, R.layout.basic_section_header, mData);

        final String campus = args.getString("campus");
        final String level = args.getString("level");
        final String semester = args.getString("semester");
        final String subjectCode = args.getString("subjectCode");

        Schedule.getCourses(campus, level, semester, subjectCode).done(new AndroidDoneCallback<JSONArray>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONArray result) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject course = result.getJSONObject(i);
                        Bundle courseItem = new Bundle();
                        courseItem.putString("title", course.getString("courseNumber") + ": " + course.getString("title"));
                        courseItem.putString("subjectCode", course.getString("subject"));
                        courseItem.putString("courseNumber", course.getString("courseNumber"));
                        courseItem.putString("campus", course.getString("campusCode"));
                        courseItem.putString("semester", semester);
                        mAdapter.add(new SlideMenuItem(courseItem));
                    } catch (JSONException e) {
                        Log.w(TAG, "getSubjects(): " + e.getMessage());
                    }
                }
            }

        }).fail(new AndroidFailCallback<AjaxStatus>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onFail(AjaxStatus result) {
                AppUtil.showFailedLoadToast(getActivity());
            }

        });
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_soc_main, parent, false);
        Resources res = getResources();
        Bundle args = getArguments();

        if(args.getString("subject") != null) getActivity().setTitle(args.getString("subject"));

        EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);

        mListView = (ListView) v.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        return v;
    }
    
}