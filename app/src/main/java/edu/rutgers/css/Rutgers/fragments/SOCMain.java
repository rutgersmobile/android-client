package edu.rutgers.css.Rutgers.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.api.Classes;
import edu.rutgers.css.Rutgers.auxiliary.JSONAdapter;
import edu.rutgers.css.Rutgers2.R;

public class SOCMain extends Fragment {

    private static final String TAG = "SOCDisplay";

    private JSONAdapter mAdapter;
    private ListView mListView;
    private JSONArray mDataSubjects;
    private JSONArray mDataCourses;

    public SOCMain() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_soc_main, parent, false);
        Resources res = getActivity().getResources();
        Bundle args = getArguments();

        getActivity().setTitle(res.getString(R.string.soc_title));

        EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);
        mListView = (ListView) v.findViewById(R.id.list);

        mAdapter = new JSONAdapter(getActivity(), new JSONArray());
        mListView.setAdapter(mAdapter);

        Classes.getSubjects("NB", "U", "72014").done(new AndroidDoneCallback<JSONArray>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONArray result) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject bleh = new JSONObject();
                        bleh.put("title", result.optJSONObject(i).optString("description") + " (" + result.optJSONObject(i).optString("code") + ")");
                        mAdapter.add(bleh);
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
                Toast.makeText(getActivity(), R.string.failed_load, Toast.LENGTH_LONG).show();
            }

        });

        return v;
    }
}