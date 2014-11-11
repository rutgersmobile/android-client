package edu.rutgers.css.Rutgers.channels.recreation.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.recreation.model.Campus;
import edu.rutgers.css.Rutgers.channels.recreation.model.Facility;
import edu.rutgers.css.Rutgers.channels.recreation.model.Gyms;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationMain extends Fragment {

    private static final String TAG = "RecreationMain";
    public static final String HANDLE = "recreation";

    private RMenuAdapter mAdapter;
    
    public RecreationMain() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<RMenuRow> data = new ArrayList<RMenuRow>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, data);

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Gyms.getCampuses()).done(new DoneCallback<List<Campus>>() {
            @Override
            public void onDone(List<Campus> result) {
                // Populate list of facilities
                for (Campus campus : result) {
                    // Create campus header
                    mAdapter.add(new RMenuHeaderRow(campus.getTitle()));

                    // Create facility rows
                    for (Facility facility : campus.getFacilities()) {
                        Bundle rowArgs = new Bundle();
                        rowArgs.putString("title", facility.getTitle());
                        rowArgs.putString("campus", campus.getTitle());
                        rowArgs.putString("facility", facility.getTitle());
                        mAdapter.add(new RMenuItemRow(rowArgs));
                    }
                }
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }
        });

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_recreation_main, parent, false);
        final Bundle args = getArguments();

        // Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.rec_title);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getItemAtPosition(position);
                Bundle newArgs = new Bundle(clicked.getArgs());
                newArgs.putString("component", RecreationDisplay.HANDLE);

                ComponentFactory.getInstance().switchFragments(newArgs);
            }

        });
        
        return v;
    }
    
}
