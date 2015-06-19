package edu.rutgers.css.Rutgers.channels.soc.model;

import android.content.Context;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.rutgers.css.Rutgers.R;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

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

        if (convertView == null) {
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
        if (scheduleItem instanceof Course) {
            final Course course = (Course) scheduleItem;
            holder.titleTextView.setText(course.getDisplayTitle());

            if (course.isStub()) {
                holder.titleTextView.setVisibility(View.GONE);
                // Replace the stub data
                final ScheduleAdapter scheduleAdapter = this;
                AndroidDeferredManager dm = new AndroidDeferredManager();
                dm.when(ScheduleAPI.getCourse(mSOCIndex.getCampusCode(), mSOCIndex.getSemesterCode(), course.getSubject(), course.getCourseNumber())).done(new DoneCallback<Course>() {
                    @Override
                    public void onDone(Course result) {
                        course.updateFields(result);
                        scheduleAdapter.notifyDataSetChanged();
                    }
                }).fail(new FailCallback<Exception>() {
                    @Override
                    public void onFail(Exception result) {
                        LOGW(TAG, result.getMessage());
                    }
                });

                holder.creditsTextView.setVisibility(View.GONE);
                holder.sectionsTextView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                // Get the number of open/total visible sections for this course
                holder.creditsTextView.setText("credits: " + course.getCredits());
                holder.sectionsTextView.setText("sections: " + course.countOpenSections(false) + "/" + course.countTotalSections(false));
                holder.titleTextView.setVisibility(View.VISIBLE);
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

        if (mFilter != null) {
            mFilter.setSocIndex(socIndex);
        }
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
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
            if (constraint == null || constraint.toString().trim().isEmpty()) {
                synchronized (mLock) {
                    // A filter has been applied before and there is a backup list, restore it
                    if (mOriginalList != null) {
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

            // Save list of original values if it doesn't exist
            synchronized(mLock) {
                if (mOriginalList == null) {
                    if (mList.size() == 0) {
                        filterResults.values = null;
                        filterResults.count = 0;
                        return filterResults;
                    } else {
                        mOriginalList = new ArrayList<>(mList);
                    }
                }
            }

            Set<ScheduleAdapterItem> passed = new HashSet<>();
            List<String> words = new ArrayList<>(Arrays.asList(constraint.toString().trim().split(" ")));

            passed.addAll(queryAbbreviations(words));

            passed.addAll(querySubjectCode(words));

            String courseId = queryCourseCode(words);

            passed.addAll(queryValidSubjects(words, socIndex.getSubjects()));

            final List<ScheduleAdapterItem> passedCopy = new ArrayList<>(passed);
            Set<ScheduleAdapterItem> courses = new HashSet<>();
            if (passed.size() > 0) {
                if (courseId != null) {
                    for (ScheduleAdapterItem subject : passedCopy) {
                        courses.addAll(socIndex.getCoursesByCodeInSubject(subject.getCode(), courseId));
                    }
                } else if (words.size() > 0) {
                    for (ScheduleAdapterItem subject : passedCopy) {
                        for (String word : words) {
                            courses.addAll(socIndex.getCoursesByNameInSubject(subject.getCode(), word, 10));
                        }
                    }
                } else if (passed.size() == 1) {
                    courses.addAll(socIndex.getCoursesInSubject(passed.iterator().next().getCode()));
                }
            } else {
                if (courseId != null) {
                    if (words.size() > 0) {
                        for (String word : words) {
                            courses.addAll(socIndex.getCoursesByCode(courseId, word));
                        }
                    } else {
                        courses.addAll(socIndex.getCoursesByCode(courseId));
                    }
                } else {
                    if (words.size() > 0) {
                        for (String word : words) {
                            courses.addAll(socIndex.getCoursesByQuery(word));
                        }
                    }
                }
            }

            List<ScheduleAdapterItem> values = new ArrayList<>(passed);
            values.addAll(courses);

            filterResults.values = values;
            filterResults.count = passed.size();
            return filterResults;
        }

        private String queryCourseCode(List<String> words) {
            for (Iterator<String> i = words.iterator(); i.hasNext();) {
                String word = i.next();
                if (isNumericalCode(word)) {
                    i.remove();
                    return word;
                }
            }
            return null;
        }

        private List<Subject> querySubjectCode(List<String> words) {
            List<Subject> results = new ArrayList<>();
            for (Iterator<String> i = words.iterator(); i.hasNext();) {
                String word = i.next();
                if (isNumericalCode(word)) {
                    Subject subject = socIndex.getSubjectByCode(word);
                    if (subject != null) {
                        i.remove();
                        results.add(subject);
                    }
                }
            }
            return results;
        }

        private List<Subject> queryAbbreviations(List<String> words) {
            List<Subject> subjectsByAbbrev = socIndex.getSubjectsByAbbreviation(words.get(0));
            if (subjectsByAbbrev.size() != 0) {
                words.remove(0);
            }
            return subjectsByAbbrev;
        }

        private List<Subject> queryValidSubjects(List<String> words, List<Subject> subjects) {
            List<Subject> results = new ArrayList<>();
            for (Subject subject : subjects) {
                if (validSubject(words, subject)) {
                    results.add(subject);
                }
            }
            return results;
        }

        private boolean validSubject(List<String> words, Subject subject) {
            List<String> subjectWords = Arrays.asList(subject.getDescription().split(" "));
            for (String word : words) {
                if (!anyStarts(word, subjectWords)) {
                    return false;
                }
            }
            return true;
        }

        private boolean anyStarts(String word, List<String> subjectWords) {
            for (String subjectWord : subjectWords) {
                if (StringUtils.startsWithIgnoreCase(subjectWord, word)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isNumericalCode(String word) {
            if (word.length() > 3 || word.length() == 0) {
                return false;
            }
            try {
                Integer.parseInt(word);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            // If filterResults is null, leave the current list alone.
            if (filterResults == null) return;

            // Replace current list with results
            synchronized (mLock) {
                mList.clear();
                if (filterResults.values != null) mList.addAll((ArrayList<ScheduleAdapterItem>) filterResults.values);
                notifyDataSetChanged();
            }
        }
    }

    private static boolean allDigits(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) return false;
        }
        return true;
    }

}
