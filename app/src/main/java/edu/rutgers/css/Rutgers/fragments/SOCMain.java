package edu.rutgers.css.Rutgers.fragments;

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
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers2.R;

public class SOCMain extends Fragment {

    private static final String TAG = "SOCDisplay";

    private ArrayList<RMenuPart> mData;
    private RMenuAdapter mAdapter;
    private ListView mListView;

    private String mCampus = "NB";
    private String mSemester = "72014";
    private String mLevel = "U";

    private JSONArray mSemesters;

    public SOCMain() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = new ArrayList<RMenuPart>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.title_row, R.layout.basic_section_header, mData);

        Schedule.getSemesters().done(new AndroidDoneCallback<JSONObject>() {
            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONObject result) {

                try {
                    JSONArray semesters = result.getJSONArray("semesters");
                    int defaultSemester = result.getInt("defaultSemester");

                    mSemester = semesters.getString(defaultSemester);
                    mSemesters = semesters;

                    for (int i = 0; i < semesters.length(); i++) {
                        Log.v(TAG, "Got semester: " + Schedule.translateSemester(semesters.getString(i)));
                    }
                    Log.v(TAG, "Default semester: " + Schedule.translateSemester(semesters.getString(defaultSemester)));

                } catch (JSONException e) {
                    Log.w(TAG, "getSemesters(): " + e.getMessage());
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

        Schedule.getSubjects(mCampus, mLevel, mSemester).done(new AndroidDoneCallback<JSONArray>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONArray result) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject subject = result.getJSONObject(i);
                        Bundle subItem = new Bundle();
                        subItem.putString("title", subject.getString("description") + " (" + subject.getString("code") + ")");
                        subItem.putString("description", subject.getString("description"));
                        subItem.putString("code", subject.getString("code"));
                        mAdapter.add(new SlideMenuItem(subItem));
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

        getActivity().setTitle(res.getString(R.string.soc_title));

        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        final ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);

        mListView = (ListView) v.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle clicked = ((SlideMenuItem)(parent.getItemAtPosition(position))).getArgs();

                Bundle args = new Bundle();
                args.putString("component", "soccourses");
                args.putString("campus", mCampus);
                args.putString("semester", mSemester);
                args.putString("level", mLevel);
                args.putString("subject", clicked.getString("descripition"));
                args.putString("subjectCode", clicked.getString("code"));

                ComponentFactory.getInstance().switchFragments(args);
            }

        });

        // Search text listener
        filterEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set filter for list adapter
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Search clear button listener
        filterClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEditText.setText("");
            }
        });

        return v;
    }

}