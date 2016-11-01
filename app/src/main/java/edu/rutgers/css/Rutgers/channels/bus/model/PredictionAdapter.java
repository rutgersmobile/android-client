package edu.rutgers.css.Rutgers.channels.bus.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.bus.model.Prediction;

/**
 * Bus arrival time predictions adapter.
 */
public class PredictionAdapter extends ExpandableRecyclerAdapter<PredictionAdapter.ViewHolder, PredictionAdapter.PopViewHolder> {

    private static final String TAG = "PredictionAdapter";

    private static final SimpleDateFormat arriveDf12 = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat arriveDf24 = new SimpleDateFormat("H:mm", Locale.US);
    private static final Calendar cal = Calendar.getInstance(Locale.US);
    private final List<Prediction> predictions;

    private Context mContext;

    private final Handler handler = new Handler();

    @Override
    public ViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        final View itemView = LayoutInflater
            .from(parentViewGroup.getContext())
            .inflate(R.layout.row_bus_prediction_title, parentViewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public PopViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        final View itemView = LayoutInflater
            .from(childViewGroup.getContext())
            .inflate(R.layout.popdown, childViewGroup, false);
        return new PopViewHolder(itemView);
    }

    @Override
    public void onBindParentViewHolder(ViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        final Prediction prediction = (Prediction) parentListItem;
        Resources res = mContext.getResources();

        parentViewHolder.itemView.setOnClickListener(view -> {
            if (parentViewHolder.isExpanded() || prediction.getMinutes().isEmpty()) {
                collapseParent(parentListItem);
            } else {
                expandParent(parentListItem);
            }
        });

        // Set title
        parentViewHolder.titleTextView.setText(prediction.getTitle());

        // Set prediction minutes
        if (!prediction.getMinutes().isEmpty()) {
            // Reset title color to default
            parentViewHolder.titleTextView.setTextColor(res.getColor(R.color.black));
            parentViewHolder.directionTextView.setTextColor(res.getColor(R.color.black));

            // Change color of text based on how much time is remaining before the first bus arrives
            int first = prediction.getMinutes().get(0);

            if (first < 2) {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_soonest));
            } else if (first < 6) {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_soon));
            } else {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_later));
            }

            parentViewHolder.minutesTextView.setText(formatMinutes(prediction.getMinutes()));
        } else {
            // No predictions loaded - gray out all text
            parentViewHolder.titleTextView.setTextColor(res.getColor(R.color.light_gray));
            parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.light_gray));
            parentViewHolder.minutesTextView.setText(R.string.bus_no_predictions);
        }

        // Set direction
        if (prediction.getDirection() == null || prediction.getDirection().isEmpty()) {
            // If there's no direction string, don't let that text field take up space
            parentViewHolder.directionTextView.setVisibility(View.GONE);
        } else {
            parentViewHolder.directionTextView.setText(prediction.getDirection());
            parentViewHolder.directionTextView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindChildViewHolder(PopViewHolder childViewHolder, int position, Object childListItem) {
        final List<Integer> minutes = (List<Integer>) childListItem;
        childViewHolder.itemView.setOnClickListener(view -> {
            final int adapterPosition = childViewHolder.getAdapterPosition();
            final ParentWrapper item = (ParentWrapper) mItemList.get(adapterPosition - 1);
            collapseParent(item.getParentListItem());
        });
        if (minutes.isEmpty()) {
            // Set pop-down contents
            childViewHolder.popdownTextView.setText(formatMinutesDetails(minutes));
            childViewHolder.popdownTextView.setGravity(Gravity.END);
        } else {
            // No predictions loaded
            childViewHolder.popdownTextView.setText("");
        }
    }

    public void addAll(Collection<? extends Prediction> predictions) {
        final int currentSize = this.predictions.size();
        this.predictions.addAll(predictions);
        notifyParentItemRangeInserted(currentSize, currentSize + predictions.size());
    }

    public void clear() {
        final int currentSize = this.predictions.size();
        predictions.clear();
        notifyParentItemRangeRemoved(0, currentSize);
    }

    static class ViewHolder extends ParentViewHolder {
        TextView titleTextView;
        TextView directionTextView;
        TextView minutesTextView;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            directionTextView = (TextView) itemView.findViewById(R.id.direction);
            minutesTextView = (TextView) itemView.findViewById(R.id.minutes);
        }
    }

    static class PopViewHolder extends ChildViewHolder {
        TextView popdownTextView;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public PopViewHolder(View itemView) {
            super(itemView);
            popdownTextView = (TextView) itemView.findViewById(R.id.popdownTextView);
        }
    }
    
    public PredictionAdapter(Context context, List<Prediction> objects) {
        super(objects);
        mContext = context;
        predictions = objects;
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
