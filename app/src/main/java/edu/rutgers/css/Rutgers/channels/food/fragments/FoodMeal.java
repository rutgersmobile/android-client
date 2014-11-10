package edu.rutgers.css.Rutgers.channels.food.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.food.model.Dining;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Displays all food items available for a specific meal at a specific dining location.
 * @author James Chambers
 */
public class FoodMeal extends Fragment {

    private static final String TAG = "FoodMeal";
    public static final String HANDLE = "foodmeal";

    private List<RMenuRow> foodItems;
    private RMenuAdapter foodItemAdapter;

    public FoodMeal() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        
        foodItems = new ArrayList<RMenuRow>();
        foodItemAdapter = new RMenuAdapter(this.getActivity(), R.layout.row_title, R.layout.row_section_header, foodItems);

        if(args.getString("location") == null) {
            Log.e(TAG, "Location not set");
            return;
        } else if(args.getString("meal") == null) {
            Log.e(TAG, "Meal not set");
            return;
        }

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Dining.getDiningLocation(args.getString("location"))).done(new DoneCallback<DiningMenu>() {

            @Override
            public void onDone(DiningMenu diningMenu) {
                DiningMenu.Meal meal = diningMenu.getMeal(args.getString("meal"));
                if (meal == null) {
                    Log.e(TAG, "Meal \"" + args.getString("meal") + "\" not found");
                    return;
                }

                // Populate the menu with categories and food items
                List<DiningMenu.Genre> mealGenres = meal.getGenres();
                for (DiningMenu.Genre genre : mealGenres) {
                    // Add category header
                    foodItemAdapter.add(new RMenuHeaderRow(genre.getGenreName()));

                    // Add food items
                    List<String> items = genre.getItems();
                    for (String item : items) {
                        foodItemAdapter.add(new RMenuItemRow(item));
                    }
                }
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, e.getMessage());
                AppUtil.showFailedLoadToast(getActivity());
            }

        });
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_food_meal, container, false);

        ListView listView = (ListView) v.findViewById(R.id.food_meal_list);
        listView.setAdapter(foodItemAdapter);
        listView.setOnItemClickListener(null);

        return v;
    }
    
}
