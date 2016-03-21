package edu.rutgers.css.Rutgers.channels.food.model;

import android.content.Context;
import android.support.annotation.NonNull;

import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SectionedListAdapter;

/**
 * Sectioned adapter for dining hall menus. The sections are the food categories and the
 * items are the food items.
 */
public class DiningMenuAdapter extends SectionedListAdapter<DiningMenu.Genre, String> {

    public DiningMenuAdapter(@NonNull Context context, int itemResource, int headerResource, int textViewId) {
        super(context, itemResource, headerResource, textViewId);
    }

    @Override
    public String getSectionHeader(DiningMenu.Genre section) {
        return section.getGenreName();
    }

    @Override
    public String getSectionItem(DiningMenu.Genre section, int position) {
        return section.getItems().get(position);
    }

    @Override
    public int getSectionItemCount(DiningMenu.Genre section) {
        return section.getItems().size();
    }

}
