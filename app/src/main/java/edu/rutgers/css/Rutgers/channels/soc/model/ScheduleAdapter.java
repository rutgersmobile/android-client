package edu.rutgers.css.Rutgers.channels.soc.model;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.Section;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Adapter for subjects and courses.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private static final String TAG = "ScheduleAdapter";

    private final List<Course> courses;
    private final int itemResource;

    private final PublishSubject<Course> coursePublishSubject = PublishSubject.create();

    public Observable<Course> getPositionClicks() {
        return coursePublishSubject.asObservable();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        TextView creditsTextView;
        TextView sectionsTextView;
        LinearLayout sectionsDisplay;
        LinearLayout creditsDisplay;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            creditsTextView = (TextView) itemView.findViewById(R.id.credits);
            sectionsTextView = (TextView) itemView.findViewById(R.id.sections);
            creditsDisplay = (LinearLayout) itemView.findViewById(R.id.credits_display);
            sectionsDisplay = (LinearLayout) itemView.findViewById(R.id.sections_display);
        }
    }

    public Course getItem(int position) {
        return courses.get(position);
    }

    public ScheduleAdapter(List<Course> courses, int itemResource) {
        super();
        this.courses = courses;
        this.itemResource = itemResource;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(itemResource, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Course course = getItem(position);
        holder.itemView.setOnClickListener(view -> coursePublishSubject.onNext(course));
        holder.creditsTextView.setText(String.valueOf(course.getCredits()));
        holder.sectionsTextView.setText(formatSections(course.getSections()));
        holder.titleTextView.setTypeface(null, Typeface.BOLD);
        holder.titleTextView.setText(RutgersUtils.formatSubject(course.getDisplayTitle()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    /**
     * Remove all elements in both lists.
     */
    public void clear() {
        courses.clear();
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Course> courses) {
        this.courses.addAll(courses);
        notifyDataSetChanged();
    }

    private static String formatSections(List<Section> sections) {
        int open = 0;
        for (final Section section : sections) {
            if (section.isOpen()) {
                open++;
            }
        }

        return open + " / " + sections.size();
    }
}
