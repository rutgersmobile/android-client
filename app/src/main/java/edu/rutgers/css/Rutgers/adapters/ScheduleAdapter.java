package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.items.SOCIndex;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 7/24/14.
 */
public class ScheduleAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ScheduleAdapter";

    private int mRowLayoutResId;
    private List<JSONObject> mList;
    private List<JSONObject> mOriginalList;
    private ScheduleFilter mFilter;
    private SOCIndex mSOCIndex;
    private final Object mLock = new Object();

    static class ViewHolder {
        TextView titleTextView;
        TextView creditsTextView;
        TextView sectionsTextView;
        ProgressBar progressBar;
    }

    public ScheduleAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        this.mRowLayoutResId = resource;
        this.mList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(mRowLayoutResId, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            holder.creditsTextView = (TextView) convertView.findViewById(R.id.credits);
            holder.sectionsTextView = (TextView) convertView.findViewById(R.id.sections);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final JSONObject jsonObject = getItem(position);

        // If it's a course
        if(jsonObject.has("courseNumber")) {
            holder.titleTextView.setText(Schedule.courseLine(jsonObject));

            if(jsonObject.optBoolean("stub") && !jsonObject.optBoolean("failedLoad")) {
                // Replace the stub JSON data
                final ScheduleAdapter scheduleAdapter = this;
                AndroidDeferredManager dm = new AndroidDeferredManager();
                dm.when(Schedule.getCourse(mSOCIndex.getCampusCode(), mSOCIndex.getSemesterCode(), jsonObject.optString("subjectCode"), jsonObject.optString("courseNumber"))).done(new DoneCallback<JSONObject>() {

                    @Override
                    public void onDone(JSONObject result) {
                        Iterator<String> keys = result.keys();
                        while(keys.hasNext()) {
                            String curKey = keys.next();
                            try {
                                jsonObject.put(curKey, result.opt(curKey));
                                jsonObject.put("stub", false);
                            } catch(JSONException e) {
                                Log.w(TAG, "getView(): " + e.getMessage());
                            }
                        }
                        scheduleAdapter.notifyDataSetChanged();
                    }

                }).fail(new FailCallback<AjaxStatus>() {

                    @Override
                    public void onFail(AjaxStatus result) {
                        Log.w(TAG, AppUtil.formatAjaxStatus(result));
                        try {
                            jsonObject.put("failedLoad", true);
                        } catch (JSONException e) {
                            Log.w(TAG, "getView(): " + e.getMessage());
                        }
                    }

                });

                holder.creditsTextView.setVisibility(View.GONE);
                holder.sectionsTextView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
            }
            else {
                // Get the number of open/total visible sections for this course
                int[] counts = Schedule.countVisibleSections(jsonObject);
                holder.creditsTextView.setText("credits: " + jsonObject.optInt("credits"));
                holder.sectionsTextView.setText("sections: " + counts[0] + "/" + counts[1]);
                holder.creditsTextView.setVisibility(View.VISIBLE);
                holder.sectionsTextView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
            }
        }
        // Assume it's a subject and hide the extra fields
        else {
            holder.titleTextView.setText(Schedule.subjectLine(jsonObject));
            holder.creditsTextView.setVisibility(View.GONE);
            holder.sectionsTextView.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
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

            // If constraint is null/empty, return original list
            if(constraint == null || constraint.toString().trim().isEmpty()) {
                synchronized (mLock) {
                    // A filter has been applied before and there is a backup list, restore it
                    if(mOriginalList != null) {
                        filterResults.values = mOriginalList;
                        filterResults.count = mOriginalList.size();
                        return filterResults;
                    }
                    // No non-empty filter has been applied so far, leave the current list untouched
                    else {
                        return null;
                    }
                }
            }

            ArrayList<JSONObject> tempList; //will be a copy of original list

            // Save list of original values if it doesn't exist
            synchronized(mLock) {
                if (mOriginalList == null) {
                    if (mList.size() == 0) {
                        filterResults.values = null;
                        filterResults.count = 0;
                        return filterResults;
                    }
                    else mOriginalList = new ArrayList<JSONObject>(mList);
                }

                tempList = new ArrayList<JSONObject>(mOriginalList);
            }

            ArrayList<JSONObject> passed = new ArrayList<JSONObject>();
            String query = constraint.toString().trim();

            // Consult the INDEX!!!
            if(socIndex != null) {
                // Check if it's a full course code 000:000
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

            // Straight string comparison on original list
            for(JSONObject jsonObject: tempList) {
                String cmp;
                if (jsonObject.has("courseNumber")) cmp = Schedule.courseLine(jsonObject);
                else cmp = Schedule.subjectLine(jsonObject);
                if (StringUtils.containsIgnoreCase(cmp, query)) passed.add(jsonObject);
            }

            // If we didn't find anything.. CONSULT THE INDEX!! to search full course names
            if(passed.isEmpty() && socIndex != null) {
                List<JSONObject> fuzzies = socIndex.getCoursesByName(query, 10);
                passed.addAll(fuzzies);
            }

            filterResults.values = passed;
            filterResults.count = passed.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            // If filterResults is null, leave the current list alone.
            if(filterResults == null) return;

            // Replace current list with results
            synchronized (mLock) {
                mList.clear();
                if (filterResults.values != null) mList.addAll((ArrayList<JSONObject>) filterResults.values);
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
