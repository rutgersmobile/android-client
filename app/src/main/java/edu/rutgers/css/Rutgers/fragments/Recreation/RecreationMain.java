package edu.rutgers.css.Rutgers.fragments.Recreation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationMain extends Fragment {

    private static final String TAG = "RecreationMain";
    public static final String HANDLE = "recreation";
    
    private ArrayList<RMenuRow> mData;
    private RMenuAdapter mAdapter;
    
    public RecreationMain() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mData = new ArrayList<RMenuRow>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Gyms.getGyms()).done(new DoneCallback<JSONArray>() {

            @Override
            public void onDone(JSONArray gymsJson) {
                try {
                    for (int i = 0; i < gymsJson.length(); i++) {
                        JSONObject campus = gymsJson.getJSONObject(i);

                        // Create campus header
                        mAdapter.add(new RMenuHeaderRow(campus.getString("title")));

                        // Create facility rows
                        JSONArray facilities = campus.getJSONArray("facilities");
                        for (int j = 0; j < facilities.length(); j++) {
                            JSONObject facility = facilities.getJSONObject(j);
                            Bundle rowArgs = new Bundle();
                            rowArgs.putString("title", facility.getString("title"));
                            rowArgs.putString("campus", campus.getString("title"));
                            rowArgs.putString("facility", facility.getString("title"));
                            mAdapter.add(new RMenuItemRow(rowArgs));
                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "onCreate(): " + e.getMessage());
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                Log.w(TAG, status.getMessage());
            }

        });
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_recreation_main, parent, false);
        Bundle args = getArguments();

        // Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.rec_title);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getItemAtPosition(position);
                Bundle args = clicked.getArgs();
                args.putString("component", RecreationDisplay.HANDLE);

                ComponentFactory.getInstance().switchFragments(args);
            }

        });
        
        return v;
    }
    
}
