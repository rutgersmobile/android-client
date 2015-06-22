package edu.rutgers.css.Rutgers.channels.soc.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.rutgers.css.Rutgers.R;

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

        holder.titleTextView.setText(scheduleItem.getDisplayTitle());
        holder.creditsTextView.setVisibility(View.GONE);
        holder.sectionsTextView.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);

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

            // All the subjects that will be returned in filterResults
            Set<Subject> subjects = new LinkedHashSet<>();

            // Words in the query tokenized by spaces and colons (for format like "198:111")
            List<String> words = new ArrayList<>(Arrays.asList(constraint.toString().trim().split("[ :]")));

            // Add subjects by abbreviations
            subjects.addAll(queryAbbreviations(words));

            // Check if we have a valid subject code in the query
            Subject subjectCode = querySubjectCode(words);
            if (subjectCode != null) {
                subjects.add(subjectCode);
            }

            // Record a valid course code if we find one in the query
            String courseId = queryCourseCode(words);

            // Add subjects that are made up of all the words in the query
            subjects.addAll(queryValidSubjects(words, socIndex.getSubjects()));

            // All courses to be returned in filterResults
            Set<Course> courses = new LinkedHashSet<>();
            if (!subjects.isEmpty()) {
                if (courseId != null) {
                    // If a subject and a course id are entered,
                    // add the matching courses from those subjects
                    courses.addAll(socIndex.getCoursesByCodeInSubjects(subjects, courseId));
                } else if (!words.isEmpty()) {
                    // Otherwise find matching course names in those subjects
                    courses.addAll(socIndex.getCoursesByNameInSubjects(subjects, words, 10));
                } else if (subjects.size() == 1) {
                    // If the only thing entered was a single subject,
                    // then get all the courses for that subject
                    courses.addAll(socIndex.getCoursesInSubject(subjects.iterator().next().getCode()));
                }
            } else {
                // If no subjects were input, get all courses matching
                // the ones put in regardless of subject
                courses.addAll(socIndex.getCoursesByCode(courseId, words));
            }

            // Smash the subjects and courses together
            // TODO Keep the lists separate
            List<ScheduleAdapterItem> values = new ArrayList<>();
            values.addAll(subjects);
            values.addAll(courses);
            filterResults.values = values;

            filterResults.count = subjects.size();
            return filterResults;
        }

        /**
         * Get subjects by abbreviations from the first word in the query.
         * Remove that word if there is a match.
         * @param words List of words in the query.
         * @return All subjects matching the given abbreviation.
         */
        private List<Subject> queryAbbreviations(List<String> words) {
            List<Subject> subjectsByAbbrev = socIndex.getSubjectsByAbbreviation(words.get(0));
            if (!subjectsByAbbrev.isEmpty()) {
                words.remove(0);
            }
            return subjectsByAbbrev;
        }

        /**
         * Find the first complete subject code and return the corresponding subject.
         * Remove the word that was matched.
         * @param words List of words in the query
         * @return A subject object if one is found, null otherwise
         */
        @Nullable
        private Subject querySubjectCode(List<String> words) {
            for (Iterator<String> i = words.iterator(); i.hasNext();) {
                String word = i.next();
                if (isNumericalCode(word)) {
                    Subject subject = socIndex.getSubjectByCode(word);
                    if (subject != null) {
                        i.remove();
                        return subject;
                    }
                }
            }
            return null;
        }

        /**
         * Find the first valid course code and return it. Remove the matched word.
         * @param words List of words in the query
         * @return String of the course code
         */
        @Nullable
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
            if (words.isEmpty()) {
                return false;
            }
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
                // TODO make this equals actually work
                if (!mList.equals(filterResults.values)) {
                    mList.clear();
                    if (filterResults.values != null)
                        mList.addAll((ArrayList<ScheduleAdapterItem>) filterResults.values);
                    notifyDataSetChanged();
                }
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
