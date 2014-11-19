package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays dining halls that have menus available in the Dining API.
 * @author James Chambers
 */
public class FoodMain extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "FoodMain";
    public static final String HANDLE = "food";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private RMenuAdapter mAdapter;
    private boolean mLoading;

    /* View references */
    private ProgressBar mProgressCircle;

    public FoodMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for dining hall listing. */
    public static Bundle createArgs(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<RMenuRow> data = new ArrayList<>(4);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title_centered, R.layout.row_section_header_centered, data);

        // Get user's home campus
        final String userHome = RutgersUtils.getHomeCampus(getActivity());

        // getString() in callback can cause crashes - load Resource strings here
        final String nbCampusFullString = getString(R.string.campus_nb_full);
        final String nwkCampusFullString = getString(R.string.campus_nwk_full);
        final String camCampusFullString = getString(R.string.campus_cam_full);

        // Static dining entries
        Bundle nwkRow = TextDisplay.createArgs(getString(R.string.dining_stonsby_title), getString(R.string.dining_stonsby_description));
        final ArrayList<RMenuRow> nwkRows = new ArrayList<>(2);
        nwkRows.add(new RMenuHeaderRow(nwkCampusFullString));
        nwkRows.add(new RMenuItemRow(nwkRow));

        Bundle camRow = TextDisplay.createArgs(getString(R.string.dining_gateway_title), getString(R.string.dining_gateway_description));
        final ArrayList<RMenuRow> camRows = new ArrayList<>(2);
        camRows.add(new RMenuHeaderRow(camCampusFullString));
        camRows.add(new RMenuItemRow(camRow));

        // Get dining hall data and populate the top-level menu with names of the dining halls
        mLoading = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(DiningAPI.getDiningHalls()).done(new DoneCallback<List<DiningMenu>>() {

            @Override
            public void onDone(List<DiningMenu> diningMenus) {
                // Temporary NB results holder
                List<RMenuRow> nbResults = new ArrayList<>();
                nbResults.add(new RMenuHeaderRow(nbCampusFullString));

                // Add dining halls - if they have no active meals, make them unclickable
                for (DiningMenu diningMenu : diningMenus) {
                    RMenuItemRow menuItemRow = new RMenuItemRow(FoodHall.createArgs(diningMenu.getLocationName()));

                    if (!diningMenu.hasActiveMeals()) {
                        menuItemRow.setClickable(false);
                        menuItemRow.setColorResId(R.color.light_gray);
                    } else {
                        menuItemRow.setClickable(true);
                    }

                    nbResults.add(menuItemRow);
                }

                // Determine campus ordering
                if (userHome.equals(nwkCampusFullString)) {
                    mAdapter.addAll(nwkRows);
                    mAdapter.addAll(camRows);
                    mAdapter.addAll(nbResults);
                } else if (userHome.equals(camCampusFullString)) {
                    mAdapter.addAll(camRows);
                    mAdapter.addAll(nwkRows);
                    mAdapter.addAll(nbResults);
                } else {
                    mAdapter.addAll(nbResults);
                    mAdapter.addAll(camRows);
                    mAdapter.addAll(nwkRows);
                }
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }
        }).always(new AlwaysCallback<List<DiningMenu>, Exception>() {
            @Override
            public void onAlways(Promise.State state, List<DiningMenu> resolved, Exception rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_food_main, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if(mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if(args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.dining_title);

        ListView listView = (ListView) v.findViewById(R.id.dining_locations_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuRow clickedRow = (RMenuRow) parent.getAdapter().getItem(position);
                if(clickedRow instanceof RMenuItemRow) {
                    ComponentFactory.getInstance().switchFragments(((RMenuItemRow) clickedRow).getArgs());
                }
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
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
