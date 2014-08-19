package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 7/25/14.
 */
public class CourseSectionAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "CourseSectionAdapter";

    // IDs for view types
    private static final int DESCRIPTION_TYPE = 0;
    private static final int PREREQ_TYPE = 1;
    private static final int MEETTIME_TYPE =2 ;

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

    // Assign numeric values to days of the week, for sorting meeting times
    private static final Map<String, Integer> dayMap =
            Collections.unmodifiableMap(new HashMap<String, Integer>() {{
                put("M", 0);
                put("T", 1);
                put("W", 2);
                put("TH", 3);
                put("F", 4);
                put("S", 5);
            }});

    public CourseSectionAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;

        resolvedGreen = context.getResources().getColor(R.color.pale_green);
        resolvedRed = context.getResources().getColor(R.color.pale_red);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        JSONObject item = getItem(position);
        if(item.optBoolean("isDescriptionRow")) return DESCRIPTION_TYPE;
        else if(item.optBoolean("isPreReqRow")) return PREREQ_TYPE;
        else return MEETTIME_TYPE;
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
        switch(getItemViewType(position)) {
            case DESCRIPTION_TYPE:
                return getDescView(position, convertView, parent);

            case PREREQ_TYPE:
                return getPreReqView(position, convertView, parent);

            case MEETTIME_TYPE:
            default:
                return getSectionView(position, convertView, parent);
        }
    }

    /**
     * Get course description row view
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getDescView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DescViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_title, null);
            holder = new DescViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }
        else {
            holder = (DescViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = getItem(position);
        holder.titleTextView.setText(StringUtils.remove(jsonObject.optString("courseDescription"), '\n'));

        return convertView;
    }

    /**
     * Get pre-requisite row view
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getPreReqView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DescViewHolder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_title, null);
            holder = new DescViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }
        else {
            holder = (DescViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = getItem(position);
        holder.titleTextView.setText("Prerequisites");

        return convertView;
    }

    /**
     * Get section row
     * @param position
     * @param convertView
     * @param parent
     * @return
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
        }
        else {
            holder = (MeetTimeViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = getItem(position);

        if(jsonObject.optBoolean("openStatus")) {
            convertView.setBackgroundColor(resolvedGreen);
        }
        else {
            convertView.setBackgroundColor(resolvedRed);
        }

        // Set section number & index number
        holder.sectionIndexTextView.setText(jsonObject.optString("number") + " " + jsonObject.optString("index"));

        // List instructors
        StringBuilder instructors = new StringBuilder();
        try {
            JSONArray instructorArray = jsonObject.getJSONArray("instructors");
            for (int i = 0; i < instructorArray.length(); i++) {
                instructors.append(instructorArray.getJSONObject(i).getString("name") + "\n");
            }
        } catch (JSONException e) {
            Log.w(TAG, "getView(): " + e.getMessage());
        }
        holder.instructorTextView.setText(StringUtils.chomp(instructors.toString()));

        // Set description
        String desc = jsonObject.optString("sectionNotes");
        if(!jsonObject.isNull("sectionNotes") && !desc.isEmpty()) {
            holder.descriptionTextView.setText(desc);
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        }
        else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        // List meeting times
        holder.meetingTimesLayout.removeAllViews();
        try {
            List<JSONObject> sortedTimes = sortMeetingTimes(jsonObject.getJSONArray("meetingTimes"));
            for(JSONObject meetingTime: sortedTimes) {
                // If all the text is null, we should discard the view. Set this to true if any
                // actual text is added.
                boolean keepView = false;

                // Meeting time row (e.g. M 12:30-1:50 HCK 110)
                TableRow timeRow = (TableRow) layoutInflater.inflate(R.layout.row_course_section_time, null);
                TextView dayTextView = (TextView) timeRow.findViewById(R.id.dayTextView);
                TextView timeTextView = (TextView) timeRow.findViewById(R.id.timeTextView);
                TextView locationTextView = (TextView) timeRow.findViewById(R.id.locationTextView);

                if(!meetingTime.isNull("meetingDay")) {
                    dayTextView.setText(meetingTime.getString("meetingDay"));
                    keepView = true;
                }
                else {
                    dayTextView.setText("");
                }

                // TODO Format this
                if(!meetingTime.isNull("startTime") && !meetingTime.isNull("endTime")) {
                    timeTextView.setText(meetingTime.getString("startTime") + " - " + meetingTime.getString("endTime"));
                    keepView = true;
                }
                else {
                    timeTextView.setText("Hours by arr.");
                }

                // I LOVE JSON <3
                StringBuilder locationBuilder = new StringBuilder();
                String[] mess = new String[3];
                mess[0] = "campusAbbrev";
                mess[1] = "buildingCode";
                mess[2] = "roomNumber";

                for(int teletubbies = 0; teletubbies < mess.length; teletubbies++) {
                    if(!meetingTime.isNull(mess[teletubbies])) {
                        locationBuilder.append(meetingTime.getString(mess[teletubbies]) + " ");
                        keepView = true;
                    }
                    /*
                                ___              |\            .---.             _
                               ( o )            |'_\           \ V /            | |
                               _| |_           _| |_           _| |_           _| |_
                             .`_____`.       .`_____`.       .`_____`.       .`_____`.
                           |\ /     \ /|   |\ /     \ /|   |\ /     \ /|   |\ /     \ /|
                           |||  @ @  |||   |||  9 9  |||   |||  6 6  |||   |||  o o  |||
                           \_\   =   /_/   \_\   -   /_/   \_\   o   /_/   \_\  ._.  /_/
                            .-'-----'-.     .-'-----'-.     .-'-----'-.     .-'-----'-.
                           (_   ___   _)   (_   ___   _)   (_   ___   _)   (_   ___   _)
                             | |___| |       | |___| |       | |___| |       | |___| |
                             |       |       |       |       |       |       |       |
                             (___|___)       (___|___)       (___|___)       (___|___)
                     */
                }

                String location = StringUtils.trim(locationBuilder.toString());

                if(!location.isEmpty()) locationTextView.setText(location);

                if(keepView) holder.meetingTimesLayout.addView(timeRow);
            }
        } catch (JSONException e) {
            Log.w(TAG, "getView(): " + e.getMessage());
        }

        return convertView;
    }

    private List<JSONObject> sortMeetingTimes(JSONArray meetingTimes) {
        if(meetingTimes == null) return null;

        List<JSONObject> result = new ArrayList<JSONObject>();
        for(int i = 0; i < meetingTimes.length(); i++) {
            result.add(meetingTimes.optJSONObject(i));
        }

        Comparator<JSONObject> meetingTimeComparator = new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                if(lhs.isNull("meetingDay") && rhs.isNull("meetingDay")) return 0;
                else if(lhs.isNull("meetingDay") && !rhs.isNull("meetingDay")) return 1;
                else if(!lhs.isNull("meetingDay") && rhs.isNull("meetingDay")) return -1;
                else {
                    String lhsDay = lhs.optString("meetingDay");
                    String rhsDay = rhs.optString("meetingDay");
                    Integer l = dayMap.get(lhsDay);
                    Integer r = dayMap.get(rhsDay);
                    return l.compareTo(r);
                }
            }

        };

        Collections.sort(result, meetingTimeComparator);

        return result;
    }

}
