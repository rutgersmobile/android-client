package edu.rutgers.css.Rutgers.fragments.Food;

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

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.items.DiningMenu;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers.utils.RutgersUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays dining halls that have menus available in the Dining API.
 * @author James Chambers
 */
public class FoodMain extends Fragment {

    private static final String TAG = "FoodMain";
    public static final String HANDLE = "food";

    private ArrayList<RMenuRow> mData;
    private RMenuAdapter mAdapter;

    public FoodMain() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = new ArrayList<RMenuRow>(4);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title_centered, R.layout.row_section_header_centered, mData);

        // Get user's home campus
        final String userHome = RutgersUtil.getHomeCampus(getActivity());

        // getString() in callback can cause crashes - load Resource strings here
        final String nbCampusFullString = getString(R.string.campus_nb_full);
        final String nwkCampusFullString = getString(R.string.campus_nwk_full);
        final String camCampusFullString = getString(R.string.campus_cam_full);

        // Static dining entries
        Bundle nwkRow = new Bundle();
        nwkRow.putString("component", TextDisplay.HANDLE);
        nwkRow.putString("title", getString(R.string.dining_stonsby_title));
        nwkRow.putString("data", getString(R.string.dining_stonsby_description));
        final ArrayList<RMenuRow> nwkRows = new ArrayList<RMenuRow>(2);
        nwkRows.add(new RMenuHeaderRow(nwkCampusFullString));
        nwkRows.add(new RMenuItemRow(nwkRow));

        Bundle camRow = new Bundle();
        camRow.putString("component", TextDisplay.HANDLE);
        camRow.putString("title", getString(R.string.dining_gateway_title));
        camRow.putString("data", getString(R.string.dining_gateway_description));
        final ArrayList<RMenuRow> camRows = new ArrayList<RMenuRow>(2);
        camRows.add(new RMenuHeaderRow(camCampusFullString));
        camRows.add(new RMenuItemRow(camRow));

        // Get dining hall data and populate the top-level menu with names of the dining halls
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Dining.getDiningHalls()).done(new DoneCallback<List<DiningMenu>>() {

            @Override
            public void onDone(List<DiningMenu> diningMenus) {
                // Temporary NB results holder
                List<RMenuRow> nbResults = new ArrayList<RMenuRow>();
                nbResults.add(new RMenuHeaderRow(nbCampusFullString));

                // Add dining halls - if they have no active meals, make them unclickable
                for (DiningMenu diningMenu : diningMenus) {
                    RMenuItemRow menuItemRow = new RMenuItemRow(diningMenu.getLocationName());

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
                AppUtil.showFailedLoadToast(getActivity());
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_food_main, parent, false);
        ListView listView = (ListView) v.findViewById(R.id.dining_locations_list);
        Bundle args = getArguments();

        // Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.dining_title);

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuRow clickedRow = (RMenuRow) parent.getAdapter().getItem(position);
                if(!(clickedRow instanceof RMenuItemRow)) return;

                Bundle clickedArgs = ((RMenuItemRow) clickedRow).getArgs();

                Bundle hallArgs = new Bundle(clickedArgs);
                hallArgs.putString("location", clickedArgs.getString("title"));
                hallArgs.putString("component", FoodHall.HANDLE);

                ComponentFactory.getInstance().switchFragments(hallArgs);
            }
        });
        
        return v;
    }

}
