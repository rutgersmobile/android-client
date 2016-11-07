package edu.rutgers.css.Rutgers.channels.soc.model;

import android.widget.Filter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.rutgers.css.Rutgers.api.model.soc.Registerable;
import edu.rutgers.css.Rutgers.api.model.soc.Course;
import edu.rutgers.css.Rutgers.api.model.soc.SOCIndex;
import edu.rutgers.css.Rutgers.api.model.soc.Subject;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Adapter for holding Subjects and Courses
 */
public class SectionedScheduleAdapter extends SimpleSectionedRecyclerAdapter<Registerable> {
    private final Object mLock = new Object();
    private ScheduleFilter mFilter;
    private SOCIndex mSOCIndex;
    private final SimpleSection<Registerable> subjects;
    private final SimpleSection<Registerable> courses;

    public SectionedScheduleAdapter(SimpleSection<Registerable> subjects, SimpleSection<Registerable> courses,
                                    int headerResource, int itemResource, int textViewId) {
        super(new ArrayList<>(), headerResource, itemResource, textViewId);
        super.add(subjects);
        super.add(courses);
        this.subjects = subjects;
        this.courses = courses;
    }

    @Override
    public void clear() {
        subjects.getItems().clear();
        courses.getItems().clear();
        notifyDataSetChanged();
    }

    @Override
    protected String getItemRepresentation(Registerable item) {
        return RutgersUtils.formatSubject(item.getDisplayTitle());
    }

    public void addAllSubjects(Collection<? extends Subject> subjects) {
        this.subjects.getItems().addAll(subjects);
    }

    public void addAllCourses(Collection<? extends Course> courses) {
        this.courses.getItems().addAll(courses);
    }

    public void setFilterIndex(SOCIndex index) {
        this.mSOCIndex = index;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ScheduleFilter();
        }

        return mFilter;
    }

    private class ScheduleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            // If constraint is null/empty, return original list
            if (StringUtils.isBlank(constraint)) {
                filterResults.values = null;
                filterResults.count = 0;
                return filterResults;
            }

            // All the subjects that will be returned in filterResults
            Set<Subject> subjects = new HashSet<>();

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
            subjects.addAll(queryValidSubjects(words, mSOCIndex.getSubjects()));

            List<Subject> subjectList = new ArrayList<>(subjects);
            Collections.sort(subjectList);
            subjects = new LinkedHashSet<>(subjectList);

            // All courses to be returned in filterResults
            Set<Course> courses = new LinkedHashSet<>();
            if (!subjects.isEmpty()) {
                if (courseId != null) {
                    // If a subject and a course id are entered,
                    // add the matching courses from those subjects
                    courses.addAll(mSOCIndex.getCoursesByCodeInSubjects(subjects, courseId));
                } else if (!words.isEmpty()) {
                    // Otherwise find matching course names in those subjects
                    courses.addAll(mSOCIndex.getCoursesByNameInSubjects(subjects, words, 10));
                } else if (subjects.size() == 1) {
                    // If the only thing entered was a single subject,
                    // then get all the courses for that subject
                    courses.addAll(mSOCIndex.getCoursesInSubject(subjects.iterator().next().getCode()));
                }
            } else {
                // If no subjects were input, get all courses matching
                // the ones put in regardless of subject
                courses.addAll(mSOCIndex.getCoursesByCodes(courseId, words));
            }

            List<FilteredSection<Registerable>> results = new ArrayList<>();
            FilteredSection<Registerable> filteredSubjects = new FilteredSection<>(
                SectionedScheduleAdapter.this.subjects,
                new ArrayList<>(subjects)
            );
            FilteredSection<Registerable> filteredCourses = new FilteredSection<>(
                SectionedScheduleAdapter.this.courses,
                new ArrayList<>(courses)
            );

            results.add(filteredSubjects);
            results.add(filteredCourses);

            filterResults.values = results;
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
            List<Subject> subjectsByAbbrev = mSOCIndex.getSubjectsByAbbreviation(words.get(0));
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
        private Subject querySubjectCode(List<String> words) {
            for (Iterator<String> i = words.iterator(); i.hasNext();) {
                String word = i.next();
                if (isNumericalCode(word)) {
                    Subject subject = mSOCIndex.getSubjectByCode(word);
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
                if (filterResults.values != null) {
                    List<FilteredSection<Registerable>> subjectsAndCourses = (List<FilteredSection<Registerable>>) filterResults.values;
                    setFilteredSections(subjectsAndCourses);
                } else {
                    setFilteredSections(null);
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
