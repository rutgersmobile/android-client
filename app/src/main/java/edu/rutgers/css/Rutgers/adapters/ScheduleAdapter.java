package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.items.SOCIndex;
import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 7/24/14.
 */
public class ScheduleAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ScheduleAdapter";

    private int layoutResource;
    private List<JSONObject> mList;
    private List<JSONObject> mOriginalList;
    private ScheduleFilter mFilter;
    private SOCIndex mSOCIndex;
    private final Object mLock = new Object();

    static class ViewHolder {
        TextView titleTextView;
        TextView creditsTextView;
        TextView sectionsTextView;
    }

    public ScheduleAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;
        this.mList = objects;
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

            holder.titleTextView.setText(Schedule.courseLine(jsonObject));
            holder.creditsTextView.setText("credits: " + jsonObject.optInt("credits"));
            holder.sectionsTextView.setText("sections: " + counts[0] + "/" + counts[1]);
            holder.creditsTextView.setVisibility(View.VISIBLE);
            holder.sectionsTextView.setVisibility(View.VISIBLE);
        }
        // Assume it's a subject and hide the extra fields
        else {
            holder.titleTextView.setText(Schedule.subjectLine(jsonObject));
            holder.creditsTextView.setVisibility(View.GONE);
            holder.sectionsTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setFilterIndex(SOCIndex socIndex) {
        mSOCIndex = socIndex;

        if(mFilter != null) {
            mFilter.setSocIndex(socIndex);
        }
    }

    @Override
    public Filter getFilter() {
        if(mFilter == null) {
            mFilter = new ScheduleFilter();
            mFilter.setSocIndex(mSOCIndex);
        }
        return mFilter;
    }

    private class ScheduleFilter extends Filter {

        SOCIndex socIndex;

        public void setSocIndex(SOCIndex socIndex) {
            this.socIndex = socIndex;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            // Copy list of original values
            if(mOriginalList == null) {
                synchronized(mLock) {
                    mOriginalList = new ArrayList<JSONObject>(mList);
                }
            }

            ArrayList<JSONObject> tempList;
            synchronized (mLock) {
                tempList = new ArrayList<JSONObject>(mOriginalList);
            }

            if(constraint == null || constraint.toString().isEmpty()) {
                filterResults.values = tempList;
                filterResults.count = tempList.size();
            }
            else {
                ArrayList<JSONObject> passed = new ArrayList<JSONObject>();
                String query = constraint.toString().trim();

                // Consult the INDEX!!!
                if(socIndex != null) {
                    // Check if its full course code 000:000
                    if(query.length() == 7 && query.charAt(3) == ':') {
                        String subjCode = query.substring(0,3);
                        String courseCode = query.substring(4,7);
                        if(allDigits(subjCode) && allDigits(courseCode)) {
                            JSONObject course = socIndex.getCourseByCode(subjCode, courseCode);
                            if(course != null) passed.add(course);
                        }
                    }
                    else {
                        // Check abbreviations
                        passed.addAll(socIndex.getSubjectsByAbbreviation(query.toUpperCase()));
                    }
                }

                // Straight string compare
                for(JSONObject jsonObject: tempList) {
                    String cmp;
                    if(jsonObject.has("courseNumber")) cmp = Schedule.courseLine(jsonObject);
                    else cmp = Schedule.subjectLine(jsonObject);
                    if(StringUtils.containsIgnoreCase(cmp, query)) passed.add(jsonObject);
                }
                filterResults.values = passed;
                filterResults.count = passed.size();
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            synchronized (mLock) {
                mList.clear();
                mList.addAll((ArrayList<JSONObject>) filterResults.values);
                notifyDataSetChanged();
            }
        }
    }

    private static boolean allDigits(String string) {
        for(int i = 0; i < string.length(); i++) {
            if(!Character.isDigit(string.charAt(i))) return false;
        }
        return true;
    }

}
