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

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.items.Schedule.Course;
import edu.rutgers.css.Rutgers.items.Schedule.SOCIndex;
import edu.rutgers.css.Rutgers.items.Schedule.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers2.R;

/**
 * Adapter for subjects and courses.
 */
public class ScheduleAdapter extends ArrayAdapter<ScheduleAdapterItem> {

    private static final String TAG = "ScheduleAdapter";

    private int mRowLayoutResId;
    private List<ScheduleAdapterItem> mList;
    private List<ScheduleAdapterItem> mOriginalList;
    private ScheduleFilter mFilter;
    private SOCIndex mSOCIndex;
    private final Object mLock = new Object();

    static class ViewHolder {
        TextView titleTextView;
        TextView creditsTextView;
        TextView sectionsTextView;
        ProgressBar progressBar;
    }

    public ScheduleAdapter(Context context, int resource, List<ScheduleAdapterItem> objects) {
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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ScheduleAdapterItem scheduleItem = getItem(position);

        // If it's a course
        if(scheduleItem instanceof Course) {
            final Course course = (Course) scheduleItem;
            holder.titleTextView.setText(course.getDisplayTitle());

            if(course.isStub()) {
                // Replace the stub data
                final ScheduleAdapter scheduleAdapter = this;
                AndroidDeferredManager dm = new AndroidDeferredManager();
                dm.when(Schedule.getCourse(mSOCIndex.getCampusCode(), mSOCIndex.getSemesterCode(), course.getSubject(), course.getCourseNumber())).done(new DoneCallback<Course>() {
                    @Override
                    public void onDone(Course result) {
                        course.updateFields(result);
                        scheduleAdapter.notifyDataSetChanged();
                    }
                }).fail(new FailCallback<Exception>() {
                    @Override
                    public void onFail(Exception result) {
                        Log.w(TAG, result.getMessage());
                    }
                });

                holder.creditsTextView.setVisibility(View.GONE);
                holder.sectionsTextView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                // Get the number of open/total visible sections for this course
                holder.creditsTextView.setText("credits: " + course.getCredits());
                holder.sectionsTextView.setText("sections: " + course.countOpenSections(false) + "/" + course.countTotalSections(false));
                holder.creditsTextView.setVisibility(View.VISIBLE);
                holder.sectionsTextView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
            }
        } else {
            // Just put in the display title (probably a subject)
            holder.titleTextView.setText(scheduleItem.getDisplayTitle());
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

            ArrayList<ScheduleAdapterItem> tempList; //will be a copy of original list

            // Save list of original values if it doesn't exist
            synchronized(mLock) {
                if (mOriginalList == null) {
                    if (mList.size() == 0) {
                        filterResults.values = null;
                        filterResults.count = 0;
                        return filterResults;
                    } else {
                        mOriginalList = new ArrayList<ScheduleAdapterItem>(mList);
                    }
                }

                tempList = new ArrayList<ScheduleAdapterItem>(mOriginalList);
            }

            ArrayList<ScheduleAdapterItem> passed = new ArrayList<ScheduleAdapterItem>();
            String query = constraint.toString().trim();

            // Consult the INDEX!!!
            if(socIndex != null) {
                // Check if it's a full course code 000:000
                if(query.length() == 7 && query.charAt(3) == ':') {
                    String subjCode = query.substring(0,3);
                    String courseCode = query.substring(4,7);
                    if(allDigits(subjCode) && allDigits(courseCode)) {
                        Course course = socIndex.getCourseByCode(subjCode, courseCode);
                        if(course != null) passed.add(course);
                    }
                } else {
                    // Check abbreviations
                    passed.addAll(socIndex.getSubjectsByAbbreviation(query.toUpperCase()));
                }

            }

            // Straight string comparison on original list
            for(ScheduleAdapterItem item: tempList) {
                String cmp = item.getDisplayTitle();
                if (StringUtils.containsIgnoreCase(cmp, query)) passed.add(item);
            }

            // If we didn't find anything.. CONSULT THE INDEX!! to search full course names
            if(passed.isEmpty() && socIndex != null) {
                List<Course> fuzzies = socIndex.getCoursesByName(query, 10);
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
                if (filterResults.values != null) mList.addAll((ArrayList<ScheduleAdapterItem>) filterResults.values);
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
