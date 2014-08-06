package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 7/24/14.
 */
public class ScheduleAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ScheduleAdapter";

    private int layoutResource;

    static class ViewHolder {
        TextView titleTextView;
        TextView creditsTextView;
        TextView sectionsTextView;
    }

    public ScheduleAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            holder.creditsTextView = (TextView) convertView.findViewById(R.id.credits);
            holder.sectionsTextView = (TextView) convertView.findViewById(R.id.sections);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = getItem(position);

        // If it's a course
        if(jsonObject.has("courseNumber")) {
            // Get the number of open/total visible sections for this course
            int[] counts = Schedule.countVisibleSections(jsonObject);

            holder.titleTextView.setText(jsonObject.optString("courseNumber") + ": " +jsonObject.optString("title"));
            holder.creditsTextView.setText("credits: " + jsonObject.optInt("credits"));
            holder.sectionsTextView.setText("sections: " + counts[0] + "/" + counts[1]);
            holder.creditsTextView.setVisibility(View.VISIBLE);
            holder.sectionsTextView.setVisibility(View.VISIBLE);
        }
        // Assume it's a subject and hide the extra fields
        else {
            holder.titleTextView.setText(jsonObject.optString("description") + " (" + jsonObject.optString("code") + ")");
            holder.creditsTextView.setVisibility(View.GONE);
            holder.sectionsTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

}
