package edu.rutgers.css.Rutgers.channels.food.model;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;

/**
 * Adapter for listing dining hall facilities in a school.
 */
public class SchoolFacilitiesAdapter extends SimpleSectionedAdapter<DiningMenu> {

    static class ViewHolder {
        TextView textView;
    }

    public SchoolFacilitiesAdapter(@NonNull Context context, int itemResource, int headerResource, int textViewId) {
        super(context, itemResource, headerResource, textViewId);
    }

    @Override
    public String getSectionHeader(SimpleSection<DiningMenu> section) {
        return section.getHeader();
    }

    @Override
    public DiningMenu getSectionItem(SimpleSection<DiningMenu> section, int position) {
        return section.getItems().get(position);
    }

    @Override
    public int getSectionItemCount(SimpleSection<DiningMenu> section) {
        return section.getItems().size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(getItemResource(), null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(getTextViewId());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Resources res = getContext().getResources();

        DiningMenu dm = getItem(position);
        holder.textView.setText(dm.getLocationName());

        if (!dm.hasActiveMeals()) {
            holder.textView.setTextColor(res.getColor(R.color.light_gray));
        } else {
            holder.textView.setTextColor(res.getColor(R.color.black));
        }

        return convertView;
    }

}
