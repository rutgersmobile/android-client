package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.channels.food.model.SchoolFacilitiesAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.RutgersAPI;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Displays dining halls that have menus available in the Dining API.
 * @author James Chambers
 */
public class FoodMain extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "FoodMain";
    public static final String HANDLE               = "food";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private SchoolFacilitiesAdapter mAdapter;
    private boolean mLoading;

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
        setHasOptionsMenu(true);

        mAdapter = new SchoolFacilitiesAdapter(getActivity(),
                R.layout.row_title, R.layout.row_section_header, R.id.title);

        // Get user's home campus
        final String userHome = RutgersUtils.getHomeCampus(getContext());

        // Static dining hall entries
        // Prevents static entries from being grayed out
        final List<DiningMenu.Meal> dummyMeal = new ArrayList<>(1);
        dummyMeal.add(new DiningMenu.Meal("fake", true, null));

        final String nbCampusFullString = getContext().getString(R.string.campus_nb_full);
        final String nwkCampusFullString = getContext().getString(R.string.campus_nwk_full);
        final String camCampusFullString = getContext().getString(R.string.campus_cam_full);

        final List<DiningMenu> stonsby = new ArrayList<>(1);
        final String stonsbyTitle = getContext().getString(R.string.dining_stonsby_title);
        stonsby.add(new DiningMenu(stonsbyTitle, 0, dummyMeal));
        final SimpleSection<DiningMenu> newarkHalls =
            new SimpleSection<>(nwkCampusFullString, stonsby);

        final List<DiningMenu> gateway = new ArrayList<>(1);
        final String gatewayTitle = getContext().getString(R.string.dining_gateway_title);
        gateway.add(new DiningMenu(gatewayTitle, 0, dummyMeal));
        final SimpleSection<DiningMenu> camdenHalls =
            new SimpleSection<>(camCampusFullString, gateway);

        // start loading dining menus
        RutgersAPI.dining.getDiningHalls()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .map(diningMenus -> {
                final List<SimpleSection<DiningMenu>> simpleSections = new ArrayList<>();
                final SimpleSection<DiningMenu> nbHalls =
                    new SimpleSection<>(nbCampusFullString, diningMenus);

                // Determine campus ordering
                if (userHome.equals(nwkCampusFullString)) {
                    simpleSections.add(newarkHalls);
                    simpleSections.add(camdenHalls);
                    simpleSections.add(nbHalls);
                } else if (userHome.equals(camCampusFullString)) {
                    simpleSections.add(camdenHalls);
                    simpleSections.add(newarkHalls);
                    simpleSections.add(nbHalls);
                } else {
                    simpleSections.add(nbHalls);
                    simpleSections.add(camdenHalls);
                    simpleSections.add(newarkHalls);
                }

                return simpleSections;
            })
            .subscribe(simpleSections -> {
                reset();
                mAdapter.addAll(simpleSections);
            }, error -> {
                reset();
                LOGE(TAG, error.getMessage());
                AppUtils.showFailedLoadToast(getContext());
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_stickylist_progress);

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.dining_title);

        StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener((parent1, view, position, id) -> {
            DiningMenu clickedMenu = (DiningMenu) parent1.getAdapter().getItem(position);

            final String stonsbyTitle = getString(R.string.dining_stonsby_title);
            final String stonsbyDescription = getString(R.string.dining_stonsby_description);

            final String gatewayTitle = getString(R.string.dining_gateway_title);
            final String gatewayDescription = getString(R.string.dining_gateway_description);
            // Launch text display if static hall is clicked. Otherwise, regular menu display.
            if (clickedMenu.getLocationName().equals(stonsbyTitle)) {
                switchFragments(TextDisplay.createArgs(stonsbyTitle, stonsbyDescription));
            } else if (clickedMenu.getLocationName().equals(gatewayTitle)) {
                switchFragments(TextDisplay.createArgs(gatewayTitle, gatewayDescription));
            } else if (clickedMenu.hasActiveMeals()) {
                switchFragments(FoodHall.createArgs(clickedMenu.getLocationName()));
            }
        });

        return v;
    }

    public Link getLink() {
        return new Link("food", new ArrayList<>(), getLinkTitle());
    }

    private void reset() {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}
