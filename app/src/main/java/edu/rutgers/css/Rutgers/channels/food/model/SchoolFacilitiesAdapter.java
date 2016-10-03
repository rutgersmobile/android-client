package edu.rutgers.css.Rutgers.channels.food.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;

/**
 * Adapter for listing dining hall facilities in a school.
 */
public class SchoolFacilitiesAdapter extends SimpleSectionedRecyclerAdapter<DiningMenu> {
    private Context context;

    public SchoolFacilitiesAdapter(Context context,
                                   List<SimpleSection<DiningMenu>> menus,
                                   int itemResource,
                                   int headerResource,
                                   int textViewId) {
        super(menus, itemResource, headerResource, textViewId);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(SimpleSectionedRecyclerAdapter.ViewHolder holder,
                                 int section, int relativePosition, int absolutePosition) {
        final DiningMenu dm = getItem(section, relativePosition);
        holder.getTextView().setText(dm.getLocationName());
        holder.itemView.setOnClickListener(view -> getOnClickSubject().onNext(dm));

        if (!dm.hasActiveMeals()) {
            holder.getTextView().setTextColor(ContextCompat.getColor(context, R.color.light_gray));
        } else {
            holder.getTextView().setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }
}
