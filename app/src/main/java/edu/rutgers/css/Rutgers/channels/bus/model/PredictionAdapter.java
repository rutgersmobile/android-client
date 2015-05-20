package edu.rutgers.css.Rutgers.channels.bus.model;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.R;

/**
 * Bus arrival time predictions adapter.
 */
public class PredictionAdapter extends ExpandableListItemAdapter<Prediction> {

    private static final String TAG = "PredictionAdapter";

    private static final SimpleDateFormat arriveDf12 = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat arriveDf24 = new SimpleDateFormat("H:mm", Locale.US);
    private static final Calendar cal = Calendar.getInstance(Locale.US);

    private Context mContext;
    
    static class ViewHolder {
        TextView titleTextView;
        TextView directionTextView;
        TextView minutesTextView;
    }

    static class PopViewHolder {
        TextView popdownTextView;
    }
    
    public PredictionAdapter(Context context, List<Prediction> objects) {
        super(context, R.layout.row_bus_prediction, R.id.titleFrame, R.id.contentFrame, objects);
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        Prediction prediction = this.getItem(position);
        return !(prediction.getMinutes()).isEmpty();
    }

    @NonNull
    @Override
    public View getTitleView(final int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_bus_prediction_title, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            holder.directionTextView = (TextView) convertView.findViewById(R.id.direction);
            holder.minutesTextView = (TextView) convertView.findViewById(R.id.minutes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Prediction prediction = this.getItem(position);
        Resources res = mContext.getResources();

        // Set title
        holder.titleTextView.setText(prediction.getTitle());

        // Set prediction minutes
        if (!prediction.getMinutes().isEmpty()) {
            // Reset title color to default
            holder.titleTextView.setTextColor(res.getColor(R.color.black));
            holder.directionTextView.setTextColor(res.getColor(R.color.black));

            // Change color of text based on how much time is remaining before the first bus arrives
            int first = prediction.getMinutes().get(0);

            if (first < 2) {
                holder.minutesTextView.setTextColor(res.getColor(R.color.bus_soonest));
            } else if (first < 6) {
                holder.minutesTextView.setTextColor(res.getColor(R.color.bus_soon));
            } else {
                holder.minutesTextView.setTextColor(res.getColor(R.color.bus_later));
            }

            holder.minutesTextView.setText(formatMinutes(prediction.getMinutes()));
        } else {
            // No predictions loaded - gray out all text
            holder.titleTextView.setTextColor(res.getColor(R.color.light_gray));
            holder.minutesTextView.setTextColor(res.getColor(R.color.light_gray));
            holder.minutesTextView.setText(R.string.bus_no_predictions);
        }

        // Set direction
        if (prediction.getDirection() == null || prediction.getDirection().isEmpty()) {
            // If there's no direction string, don't let that text field take up space
            holder.directionTextView.setVisibility(View.GONE);
        } else {
            holder.directionTextView.setText(prediction.getDirection());
            holder.directionTextView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @NonNull
    @Override
    public View getContentView(final int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PopViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.popdown, null);
            holder = new PopViewHolder();
            holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
            convertView.setTag(holder);
        } else {
            holder = (PopViewHolder) convertView.getTag();
        }

        Prediction prediction = getItem(position);

        // Set prediction minutes
        if (!prediction.getMinutes().isEmpty()) {
            // Set pop-down contents
            holder.popdownTextView.setText(formatMinutesDetails(prediction.getMinutes()));
            holder.popdownTextView.setGravity(Gravity.RIGHT);
        } else {
            // No predictions loaded
            holder.popdownTextView.setText("");
        }

        return convertView;
    }

    /**
     * Create "Arriving in..." string, e.g.<br>
     *      Arriving in 2 minutes.<br>
     *         Arriving in 2 and 4 minutes.<br>
     *         Arriving in 2, 3, and 4 minutes.<br>
     *         Arriving in 1 minute.<br>
     * @param minutes Array of arrival times in minutes
     * @return Formatted arrival time string
     */
    private String formatMinutes(List<Integer> minutes) {
        Resources resources = mContext.getResources();
        StringBuilder result = new StringBuilder(resources.getString(R.string.bus_prediction_begin));
        
        for (int i = 0; i < minutes.size(); i++) {
            if (i != 0 && i == minutes.size() - 1) result.append(resources.getString(R.string.bus_and));
            
            if (minutes.get(i) < 1) result.append(" <1");
            else result.append(" ").append(minutes.get(i));
            
            if (minutes.size() > 2 && i != minutes.size() - 1) result.append(",");
        }
        
        if (minutes.size() == 1 && minutes.get(0) == 1) result.append(resources.getString(R.string.bus_minute_singular));
        else result.append(resources.getString(R.string.bus_minute_plural));
        
        return result.toString();
    }

    /**
     * Create pop-down details string<br/>
     * e.g. <b>10</b> minutes at <b>12:30</b>
     * @param minutes Array of arrival times in minutes
     * @return Formatted and stylized arrival time details string
     */
    private CharSequence formatMinutesDetails(List<Integer> minutes) {
        Resources resources = mContext.getResources();
        SpannableStringBuilder result = new SpannableStringBuilder();

        for (int i = 0; i < minutes.size(); i++) {
            // Hack so that it displays "<1 minute" instead of "0 minutes"
            int mins = minutes.get(i) == 0 ? 1 : minutes.get(i);
            String minString = minutes.get(i) < 1 ? "&lt;1" : Integer.toString(mins);

            // Determine bus arrival time
            Date date = new Date();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, minutes.get(i));
            String arrivalTime = android.text.format.DateFormat.is24HourFormat(mContext) ?
                    PredictionAdapter.arriveDf24.format(cal.getTime()) :
                    PredictionAdapter.arriveDf12.format(cal.getTime());

            // Get appropriate string & format, stylize
            String line = resources.getQuantityString(R.plurals.bus_minute_details, mins, minString, arrivalTime);
            CharSequence styledLine = Html.fromHtml(line);
            result.append(styledLine);
            if (i != minutes.size() - 1) result.append("\n");
        }

        return result;
    }
    
}
