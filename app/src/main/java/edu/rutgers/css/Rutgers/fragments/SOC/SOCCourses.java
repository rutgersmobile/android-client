package edu.rutgers.css.Rutgers.fragments.SOC;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;

import org.apache.commons.lang3.text.WordUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.ScheduleAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

public class SOCCourses extends Fragment {

    private static final String TAG = "SOCCourses";
    public static final String HANDLE = "soccourses";

    private List<JSONObject> mData;
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

        mData = new ArrayList<JSONObject>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, mData);

        // Restore filter
        if(savedInstanceState != null && savedInstanceState.getString("filter") != null) {
            mFilterString = savedInstanceState.getString("filter");
        }

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
                        mAdapter.add(result.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.w(TAG, "getSubjects(): " + e.getMessage());
                    }
                }

                // Re-apply filter
                mAdapter.getFilter().filter(mFilterString);
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

        if(args.getString("title") != null) getActivity().setTitle(WordUtils.capitalizeFully(args.getString("title")));

        final String semester = args.getString("semester");

        mFilterEditText = (EditText) v.findViewById(R.id.filterEditText);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject clickedJSON = (JSONObject) parent.getItemAtPosition(position);

                Bundle args = new Bundle();
                args.putString("component", SOCSections.HANDLE);
                args.putString("title", clickedJSON.optString("courseNumber") + ": " + clickedJSON.optString("title"));
                args.putString("data", clickedJSON.toString());
                args.putString("semester", semester);

                ComponentFactory.getInstance().switchFragments(args);
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
                mFilterString = s.toString();
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
                mFilterEditText.setText("");
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