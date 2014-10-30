package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.items.Schedule.Section;
import edu.rutgers.css.Rutgers.items.Schedule.SectionAdapterItem;
import edu.rutgers.css.Rutgers2.R;

/**
 * An array adapter for course sections. Displays each section with instructor and meeting times.
 * Also displays course description and prerequisites if available.
 *
 * @author James Chambers
 */
public class CourseSectionAdapter extends ArrayAdapter<SectionAdapterItem> {

    private static final String TAG = "CourseSectionAdapter";

    // IDs for view types
    private static enum ViewTypes {
        BASIC_TYPE, MEETTIME_TYPE
    }
    private static ViewTypes[] viewTypes = ViewTypes.values();

    private int layoutResource;
    private int resolvedGreen;
    private int resolvedRed;

    static class MeetTimeViewHolder {
        TextView sectionIndexTextView;
        TextView instructorTextView;
        TextView descriptionTextView;
        TableLayout meetingTimesLayout;
    }

    static class DescViewHolder {
        TextView titleTextView;
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

    public CourseSectionAdapter(Context context, int resource, List<SectionAdapterItem> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;

        resolvedGreen = context.getResources().getColor(R.color.pale_green);
        resolvedRed = context.getResources().getColor(R.color.pale_red);
    }

    @Override
    public int getViewTypeCount() {
        return viewTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        SectionAdapterItem item = getItem(position);
        if(item instanceof Section) {
            return ViewTypes.MEETTIME_TYPE.ordinal();
        } else {
            return ViewTypes.BASIC_TYPE.ordinal();
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch(viewTypes[getItemViewType(position)]) {
            case BASIC_TYPE:
                return getBasicView(position, convertView, parent);

            case MEETTIME_TYPE:
            default:
                return getSectionView(position, convertView, parent);
        }
    }

    /**
     * Get basic text row view
     */
    public View getBasicView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DescViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_title, null);
            holder = new DescViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (DescViewHolder) convertView.getTag();
        }

        SectionAdapterItem item = getItem(position);
        holder.titleTextView.setText(item.getDisplayTitle());

        return convertView;
    }

    /**
     * Get section row
     */
    public View getSectionView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MeetTimeViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new MeetTimeViewHolder();
            holder.sectionIndexTextView = (TextView) convertView.findViewById(R.id.sectionIndexTextView);
            holder.instructorTextView = (TextView) convertView.findViewById(R.id.instructorTextView);
            holder.descriptionTextView = (TextView) convertView.findViewById(R.id.descriptionTextView);
            holder.meetingTimesLayout = (TableLayout) convertView.findViewById(R.id.meetingTimesLayout);
            convertView.setTag(holder);
        } else {
            holder = (MeetTimeViewHolder) convertView.getTag();
        }

        Section section = (Section) getItem(position);

        // Open sections have a green background, closed sections have a red background
        if(section.isOpen()) {
            convertView.setBackgroundColor(resolvedGreen);
        } else {
            convertView.setBackgroundColor(resolvedRed);
        }

        // Set section number & index number
        holder.sectionIndexTextView.setText(section.getNumber() + " " + section.getIndex());

        // List instructors
        StringBuilder instructors = new StringBuilder();
        for(Section.Instructor instructor: section.getInstructors()) {
            instructors.append(instructor.getName()).append('\n');
        }
        holder.instructorTextView.setText(StringUtils.chomp(instructors.toString()));

        // Set description
        if(section.getSectionNotes() != null) {
            holder.descriptionTextView.setText(section.getSectionNotes());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        // List meeting times
        holder.meetingTimesLayout.removeAllViews();
        if(section.getMeetingTimes() != null) {
            List<Section.MeetingTime> meetingTimes = sortMeetingTimes(section.getMeetingTimes());
            for (Section.MeetingTime meetingTime : meetingTimes) {
                // If all the text is blank, we should discard the view. Set this to true if any
                // actual text is added.
                boolean keepView = false;

                // Meeting time row (e.g. M 12:30-1:50 HCK 110)
                TableRow timeRow = (TableRow) layoutInflater.inflate(R.layout.row_course_section_time, null);
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

        return convertView;
    }

    /**
     * Arrange list of meet times by day of week.
     * @param meetingTimes List of meeting times to sort
     * @return Meeting times sorted by day
     */
    private List<Section.MeetingTime> sortMeetingTimes(@NonNull List<Section.MeetingTime> meetingTimes) {
        List<Section.MeetingTime> result = new ArrayList<Section.MeetingTime>(meetingTimes);

        Comparator<Section.MeetingTime> meetingTimeComparator = new Comparator<Section.MeetingTime>() {

            @Override
            public int compare(Section.MeetingTime lhs, Section.MeetingTime rhs) {
                if(StringUtils.isBlank(lhs.getMeetingDay()) && StringUtils.isBlank(rhs.getMeetingDay())) {
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
            if(endInt == 12 || endInt < beginInt) endPmCode = "P";
        } catch (NumberFormatException e) {
            Log.w(TAG, "Couldn't parse ints");
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
