package edu.rutgers.css.Rutgers.channels.food.model;

import java.util.List;

import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;

/**
 * Sectioned adapter for dining hall menus. The sections are the food categories and the
 * items are the food items.
 */
public class DiningMenuAdapter extends SimpleSectionedRecyclerAdapter<String> {
    public DiningMenuAdapter(List<SimpleSection<String>> sections, int itemResource, int headerResource, int textViewId) {
        super(sections, itemResource, headerResource, textViewId);
    }
}
