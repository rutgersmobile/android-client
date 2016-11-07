package edu.rutgers.css.Rutgers.channels.soc.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.model.soc.Titleable;
import edu.rutgers.css.Rutgers.api.model.soc.Section;
import rx.Observable;
import rx.subjects.PublishSubject;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * An array adapter for course sections. Displays each section with instructor and meeting times.
 * Also displays course description and prerequisites if available.
 *
 * @author James Chambers
 */
public class CourseSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CourseSectionAdapter";

    private final List<Titleable> elements;
    private PublishSubject<Titleable> elementSubject = PublishSubject.create();

    public Observable<Titleable> getPositionClicks() {
        return elementSubject.asObservable();
    }

    public void add(Titleable element) {
        elements.add(element);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Titleable> elements) {
        this.elements.addAll(elements);
        notifyDataSetChanged();
    }

    public void clear() {
        elements.clear();
        notifyDataSetChanged();
    }

    // IDs for view types
    private enum ViewTypes {
        BASIC_TYPE, MEETTIME_TYPE
    }
    private static ViewTypes[] viewTypes = ViewTypes.values();

    private int layoutResource;
    private int resolvedGreen;
    private int resolvedRed;

    private static class MeetTimeViewHolder extends RecyclerView.ViewHolder {
        TextView sectionIndexTextView;
        TextView instructorTextView;
        TextView descriptionTextView;
        TableLayout meetingTimesLayout;

        MeetTimeViewHolder(View itemView) {
            super(itemView);

            sectionIndexTextView = (TextView) itemView.findViewById(R.id.sectionIndexTextView);
            instructorTextView = (TextView) itemView.findViewById(R.id.instructorTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            meetingTimesLayout = (TableLayout) itemView.findViewById(R.id.meetingTimesLayout);
        }
    }

    private static class DescViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        DescViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public Titleable getItem(int position) {
        return elements.get(position);
    }

    // Assign numeric values to days of the week, for sorting meet times
    private static final Map<String, Integer> sDayMap =
        Collections.unmodifiableMap(new HashMap<String, Integer>() {{
            put("M", 0);
            put("T", 1);
            put("W", 2);
            put("TH", 3);
            put("F", 4);
            put("S", 5);
        }});

    public CourseSectionAdapter(Context context, int resource, List<Titleable> objects) {
        super();

        this.layoutResource = resource;
        this.elements = objects;

        resolvedGreen = ContextCompat.getColor(context, R.color.pale_green);
        resolvedRed = ContextCompat.getColor(context, R.color.pale_red);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewTypes[viewType]) {
            case BASIC_TYPE:
                final View basicItemView = inflater.inflate(R.layout.row_title, parent, false);
                return new DescViewHolder(basicItemView);
            default:
                final View meetTimeItemView = inflater.inflate(layoutResource, parent, false);
                return new MeetTimeViewHolder(meetTimeItemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Titleable item = getItem(position);
        holder.itemView.setOnClickListener(view -> elementSubject.onNext(item));
        switch (viewTypes[getItemViewType(position)]) {
            case BASIC_TYPE:
                bindBasicViewHolder((DescViewHolder) holder, item);
                return;
            default:
                bindMeetTimeViewHolder((MeetTimeViewHolder) holder, item);
        }
    }

    private void bindBasicViewHolder(DescViewHolder holder, Titleable item) {
        holder.titleTextView.setText(item.getDisplayTitle());
    }

    private void bindMeetTimeViewHolder(MeetTimeViewHolder holder, Titleable item) {
        Section section = (Section) item;

        // Open sections have a green background, closed sections have a red background
        if (section.isOpen()) {
            holder.itemView.setBackgroundColor(resolvedGreen);
        } else {
            holder.itemView.setBackgroundColor(resolvedRed);
        }

        // Set section number & index number
        holder.sectionIndexTextView.setText(section.getNumber() + " " + section.getIndex());

        // List instructors
        List<Section.Instructor> instructors = section.getInstructors();
        StringBuilder instructorString = new StringBuilder();
        for (int i = 0; i < instructors.size(); i++) {
            Section.Instructor instructor = instructors.get(i);
            instructorString.append(instructor.getName());
            if (i != instructors.size() - 1) instructorString.append("; ");
        }
        holder.instructorTextView.setText(StringUtils.chomp(instructorString.toString()));

        // Set description
        if (section.getSectionNotes() != null) {
            holder.descriptionTextView.setText(section.getSectionNotes());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        // List meeting times
        holder.meetingTimesLayout.removeAllViews();
        if (section.getMeetingTimes() != null) {
            List<Section.MeetingTime> meetingTimes = sortMeetingTimes(section.getMeetingTimes());
            for (Section.MeetingTime meetingTime : meetingTimes) {
                // If all the text is blank, we should discard the view. Set this to true if any
                // actual text is added.
                boolean keepView = false;

                // Meeting time row (e.g. M 12:30-1:50 HCK 110)
                TableRow timeRow = (TableRow) LayoutInflater
                    .from(holder.itemView.getContext())
                    .inflate(R.layout.row_course_section_time, holder.meetingTimesLayout, false);
                TextView dayTextView = (TextView) timeRow.findViewById(R.id.dayTextView);
                TextView timeTextView = (TextView) timeRow.findViewById(R.id.timeTextView);
                TextView locationTextView = (TextView) timeRow.findViewById(R.id.locationTextView);

                if (meetingTime.getMeetingDay() != null) {
                    dayTextView.setText(meetingTime.getMeetingDay());
                    keepView = true;
                } else {
                    dayTextView.setText("");
                }

                // Format meeting times
                if (StringUtils.isNoneBlank(meetingTime.getStartTime(), meetingTime.getEndTime(), meetingTime.getPmCode())) {
                    timeTextView.setText(formatTimes(meetingTime.getStartTime(), meetingTime.getEndTime(), meetingTime.getPmCode()));
                    keepView = true;
                } else {
                    timeTextView.setText("Hours by arr.");
                }

                String[] locationElements = new String[]{
                    meetingTime.getCampusAbbrev(),
                    meetingTime.getBuildingCode(),
                    meetingTime.getRoomNumber()
                };

                StringBuilder locationBuilder = new StringBuilder();
                for (String element : locationElements) {
                    if (StringUtils.isNotBlank(element))
                        locationBuilder.append(element).append(' ');
                }
                String location = StringUtils.trim(locationBuilder.toString());

                if (StringUtils.isNotBlank(location)) {
                    locationTextView.setText(location);
                    keepView = true;
                }

                if (keepView) holder.meetingTimesLayout.addView(timeRow);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Titleable item = getItem(position);
        if (item instanceof Section) {
            return ViewTypes.MEETTIME_TYPE.ordinal();
        } else {
            return ViewTypes.BASIC_TYPE.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    /**
     * Arrange list of meet times by day of week.
     * @param meetingTimes List of meeting times to sort
     * @return Meeting times sorted by day
     */
    private List<Section.MeetingTime> sortMeetingTimes(@NonNull List<Section.MeetingTime> meetingTimes) {
        List<Section.MeetingTime> result = new ArrayList<>(meetingTimes);

        Comparator<Section.MeetingTime> meetingTimeComparator = (lhs, rhs) -> {
            if (StringUtils.isBlank(lhs.getMeetingDay()) && StringUtils.isBlank(rhs.getMeetingDay())) {
                return 0;
            } else if (StringUtils.isNotBlank(lhs.getMeetingDay()) && StringUtils.isBlank(rhs.getMeetingDay())) {
                return 1;
            } else if (StringUtils.isBlank(lhs.getMeetingDay()) && StringUtils.isNotBlank(rhs.getMeetingDay())) {
                return -1;
            } else {
                Integer l = sDayMap.get(lhs.getMeetingDay());
                Integer r = sDayMap.get(rhs.getMeetingDay());
                return l.compareTo(r);
            }
        };

        Collections.sort(result, meetingTimeComparator);

        return result;
    }

    /**
     * Produce a single-line string showing a time range, using the time strings supplied by WebReg.
     * @param beginTime 12-hour time string formatted like "1230"
     * @param endTime 12-hour time string formatted like "1230"
     * @param pmCode AM or PM
     * @return Time range string like "11:30 AM - 02:30 PM"
     */
    private String formatTimes(String beginTime, String endTime, String pmCode) {
        String beginHour = beginTime.substring(0,2);
        String endHour = endTime.substring(0,2);

        // We only get the AM/PM code for the beginning hour. Check if meridiem changes,
        // and adjust end code if necessary.
        String endPmCode = pmCode;
        try {
            Integer beginInt = Integer.parseInt(beginHour);
            Integer endInt = Integer.parseInt(endHour);
            if (endInt == 12 || endInt < beginInt) endPmCode = "P";
        } catch (NumberFormatException e) {
            LOGW(TAG, "Couldn't parse ints");
        }

        return beginHour + ":" + beginTime.substring(2,4) + " " + pmCode + "M" +
                " - " +
                endHour + ":" + endTime.substring(2,4) + " " + endPmCode + "M";
    }

    /*
                 .-.
                (o o) boo!
                | O \
                 \   \
                  `~~~'
    */

}
