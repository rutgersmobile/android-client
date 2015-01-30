package edu.rutgers.css.Rutgers.channels.recreation.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.recreation.model.Campus;
import edu.rutgers.css.Rutgers.channels.recreation.model.Facility;
import edu.rutgers.css.Rutgers.channels.recreation.model.FacilityAdapter;
import edu.rutgers.css.Rutgers.channels.recreation.model.GymsAPI;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Display gyms and facilities for each campus
 */
public class RecreationMain extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "RecreationMain";
    public static final String HANDLE = "recreation";

    /* Argument bundle tags */
    public static final String ARG_TITLE_TAG        = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private FacilityAdapter mAdapter;
    private boolean mLoading;

    /* View references */
    private ProgressBar mProgressCircle;

    public RecreationMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main gyms screen. */
    public static Bundle createArgs(@NonNull String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RecreationMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FacilityAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);

        mLoading = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(GymsAPI.getCampuses()).done(new DoneCallback<List<Campus>>() {
            @Override
            public void onDone(List<Campus> result) {
                mAdapter.addAll(result);
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }
        }).always(new AlwaysCallback<List<Campus>, Exception>() {
            @Override
            public void onAlways(Promise.State state, List<Campus> resolved, Exception rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stickylist_progress, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.rec_title);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Facility facility = mAdapter.getItem(position);
                Campus campus = mAdapter.getSectionContainingItem(position);

                Bundle newArgs = RecreationDisplay.createArgs(facility.getTitle(),
                        campus.getTitle(), facility.getTitle());
                ComponentFactory.getInstance().switchFragments(newArgs);
            }

        });
        
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    private void showProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
