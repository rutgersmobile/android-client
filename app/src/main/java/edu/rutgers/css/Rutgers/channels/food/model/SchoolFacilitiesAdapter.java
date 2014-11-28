package edu.rutgers.css.Rutgers.channels.food.model;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.model.SectionedListAdapter;

/**
 * Adapter for listing dining hall facilities in a school.
 */
public class SchoolFacilitiesAdapter extends SectionedListAdapter<Map.Entry<String, List<DiningMenu>>, DiningMenu> {

    static class ViewHolder {
        TextView textView;
    }

    public SchoolFacilitiesAdapter(@NonNull Context context, int itemResource, int headerResource, int textViewId) {
        super(context, itemResource, headerResource, textViewId);
    }

    @Override
    public String getSectionHeader(Map.Entry<String, List<DiningMenu>> section) {
        return section.getKey();
    }

    @Override
    public DiningMenu getSectionItem(Map.Entry<String, List<DiningMenu>> section, int position) {
        return section.getValue().get(position);
    }

    @Override
    public int getSectionItemCount(Map.Entry<String, List<DiningMenu>> section) {
        return section.getValue().size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
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

        if(!dm.hasActiveMeals()) {
            holder.textView.setTextColor(res.getColor(R.color.light_gray));
        } else {
            holder.textView.setTextColor(res.getColor(R.color.black));
        }

        return convertView;
    }

}
