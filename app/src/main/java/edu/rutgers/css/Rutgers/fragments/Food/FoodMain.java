package edu.rutgers.css.Rutgers.fragments.Food;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Dining;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays dining halls. Selecting a hall goes to meal menu.
 *
 */
public class FoodMain extends Fragment {

	private static final String TAG = "FoodMain";

    private ArrayList<RMenuRow> mData;
    private RMenuAdapter mAdapter;

	public FoodMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mData = new ArrayList<RMenuRow>(4);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);

        final String nbCampusFullString = getResources().getString(R.string.campus_nb_full);

		// Get dining hall data and populate the top-level menu with names of the dining halls
		Dining.getDiningHalls().done(new AndroidDoneCallback<JSONArray>() {

            @Override
            public void onDone(JSONArray halls) {
                try {
                    mAdapter.add(new RMenuHeaderRow(nbCampusFullString));

                    // Add dining halls - if they have no active meals, make them unclickable
                    for (int i = 0; i < halls.length(); i++) {
                        JSONObject curHall = halls.getJSONObject(i);
                        Bundle hallBundle = new Bundle();
                        hallBundle.putString("title", curHall.getString("location_name"));
                        hallBundle.putString("location", curHall.getString("location_name"));

                        if (Dining.hasActiveMeals(curHall)) {
                            mAdapter.add(new RMenuItemRow(hallBundle));
                        } else {
                            RMenuItemRow inactiveHallItem = new RMenuItemRow(hallBundle);
                            inactiveHallItem.setClickable(false);
                            inactiveHallItem.setColorResId(R.color.light_gray);
                            mAdapter.add(inactiveHallItem);
                        }
                    }

                    loadStaticHalls();
                } catch (JSONException e) {
                    Log.w(TAG, e.getMessage());
                    Toast.makeText(getActivity(), R.string.failed_internal, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

        }).fail(new AndroidFailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus e) {
                AppUtil.showFailedLoadToast(getActivity());
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

        });
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_food_main, parent, false);
		ListView listView = (ListView) v.findViewById(R.id.dining_locations_list);
		
		Bundle args = getArguments();
		if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));

		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle clickedArgs = ((RMenuItemRow) parent.getAdapter().getItem(position)).getArgs();

                Bundle args = new Bundle(clickedArgs);
                if (args.getString("component") == null) args.putString("component", "foodhall");

                ComponentFactory.getInstance().switchFragments(args);
            }

        });
		
		return v;
	}

    private void loadStaticHalls() {
        Bundle nwk = new Bundle();
        nwk.putString("component", "text");
        nwk.putString("title", "Stonsby Commons & Eatery");
        nwk.putString("data", "Students enjoy all-you-care-to-eat dining in a contemporary setting. This exciting location offers fresh made menu items, cutting-edge American entrees, ethnically-inspired foods, vegetarian selections and lots more... \n\nThe Commons also features upscale Premium entrees and fresh baked goods from our in house bakery or local vendors.");

        Bundle cam = new Bundle();
        cam.putString("component", "text");
        cam.putString("title", "Gateway Cafe");
        cam.putString("data", "The Camden Dining Hall, the Gateway Cafe, is located at the Camden Campus Center.\n\nIt offers a variety of eateries in one convenient location.");

        mAdapter.add(new RMenuHeaderRow(getResources().getString(R.string.campus_nwk_full)));
        mAdapter.add(new RMenuItemRow(nwk));
        mAdapter.add(new RMenuHeaderRow(getResources().getString(R.string.campus_cam_full)));
        mAdapter.add(new RMenuItemRow(cam));
    }
	
}
