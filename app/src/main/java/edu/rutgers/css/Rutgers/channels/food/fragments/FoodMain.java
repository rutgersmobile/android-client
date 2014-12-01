package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.SchoolFacilitiesAdapter;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

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
    private SchoolFacilitiesAdapter mAdapter;
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

        mAdapter = new SchoolFacilitiesAdapter(getActivity(),
                R.layout.row_title, R.layout.row_section_header, R.id.title);

        // Get user's home campus
        final String userHome = RutgersUtils.getHomeCampus(getActivity());

        // getString() in callback can cause crashes - load Resource strings here
        final String nbCampusFullString = getString(R.string.campus_nb_full);
        final String nwkCampusFullString = getString(R.string.campus_nwk_full);
        final String camCampusFullString = getString(R.string.campus_cam_full);

        // Static dining hall entries
        List<DiningMenu.Meal> dummyMeal = new ArrayList<>(1);
        dummyMeal.add(new DiningMenu.Meal("fake", true, null)); // Prevents static entries from being grayed out

        List<DiningMenu> stonsby = new ArrayList<>(1);
        stonsby.add(new DiningMenu(getString(R.string.dining_stonsby_title), 0, dummyMeal));
        final SimpleSection<DiningMenu> newarkHalls = new SimpleSection<>(nwkCampusFullString, stonsby);

        List<DiningMenu> gateway = new ArrayList<>(1);
        gateway.add(new DiningMenu(getString(R.string.dining_gateway_title), 0, dummyMeal));
        final SimpleSection<DiningMenu> camdenHalls = new SimpleSection<>(camCampusFullString, gateway);

        // Get dining hall data and populate the top-level menu with names of the dining halls
        mLoading = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(DiningAPI.getDiningHalls()).done(new DoneCallback<List<DiningMenu>>() {

            @Override
            public void onDone(List<DiningMenu> diningMenus) {
                SimpleSection<DiningMenu> nbHalls = new SimpleSection<>(nbCampusFullString, diningMenus);

                // Determine campus ordering
                if (userHome.equals(nwkCampusFullString)) {
                    mAdapter.add(newarkHalls);
                    mAdapter.add(camdenHalls);
                    mAdapter.add(nbHalls);
                } else if (userHome.equals(camCampusFullString)) {
                    mAdapter.add(camdenHalls);
                    mAdapter.add(newarkHalls);
                    mAdapter.add(nbHalls);
                } else {
                    mAdapter.add(nbHalls);
                    mAdapter.add(camdenHalls);
                    mAdapter.add(newarkHalls);
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
        final View v = inflater.inflate(R.layout.fragment_food_main, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if(mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if(args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.dining_title);

        StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiningMenu clickedMenu = (DiningMenu) parent.getAdapter().getItem(position);

                // Check for static halls first
                if(clickedMenu.getLocationName().equals(getString(R.string.dining_stonsby_title))) {
                    ComponentFactory.getInstance().switchFragments(
                            TextDisplay.createArgs(getString(R.string.dining_stonsby_title),
                                    getString(R.string.dining_stonsby_description))
                    );
                } else if (clickedMenu.getLocationName().equals(getString(R.string.dining_gateway_title))) {
                    ComponentFactory.getInstance().switchFragments(
                            TextDisplay.createArgs(getString(R.string.dining_gateway_title),
                                    getString(R.string.dining_gateway_description))
                    );
                } else {
                    if(clickedMenu.hasActiveMeals()) {
                        ComponentFactory.getInstance().switchFragments(
                                FoodHall.createArgs(clickedMenu.getLocationName())
                        );
                    }
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
