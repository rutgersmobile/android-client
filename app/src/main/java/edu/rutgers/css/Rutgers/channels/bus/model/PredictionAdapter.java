package edu.rutgers.css.Rutgers.channels.bus.model;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.model.bus.Prediction;
import edu.rutgers.css.Rutgers.api.model.bus.VehiclePrediction;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.link.Link;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Bus arrival time predictions adapter.
 */
public class PredictionAdapter extends ExpandableRecyclerAdapter<PredictionAdapter.ViewHolder, PredictionAdapter.PopViewHolder> {

    private static final String TAG = "PredictionAdapter";

    private static final SimpleDateFormat arriveDf12 = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat arriveDf24 = new SimpleDateFormat("H:mm", Locale.US);
    private static final Calendar cal = Calendar.getInstance(Locale.US);
    private final List<Prediction> predictions;
    private PublishSubject<Prediction> aedanPublishSubject = PublishSubject.create();
    public Observable<Prediction> getAedanClicks() {
        return aedanPublishSubject.asObservable();
    }
    private final boolean hideAedan;

    private String agency;
    private String tag;
    private String mode;

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private FragmentActivity fragmentActivity;

    private final Handler handler = new Handler();

    @Override
    public ViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        final View itemView = LayoutInflater
            .from(parentViewGroup.getContext())
            .inflate(R.layout.row_bus_prediction_title_constraint, parentViewGroup, false);
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
        Resources res = fragmentActivity.getResources();

        parentViewHolder.itemView.setOnClickListener(view -> {
            if (parentViewHolder.isExpanded() || prediction.getVehiclePredictions().isEmpty()) {
                collapseParent(parentListItem);
            } else {
                expandParent(parentListItem);
            }
        });

        parentViewHolder.alarmImageView.setOnClickListener(view -> {
            String routeTag;
            String stopTag;
            if (mode.equals(BusDisplay.STOP_MODE)) {
                routeTag = prediction.getTag();
                stopTag = tag;
            } else {
                routeTag = tag;
                stopTag = prediction.getTitle();
            }

            final List<String> pathParts = new ArrayList<>();
            pathParts.add(mode);
            pathParts.add(tag);
            final Bundle notificationDialogArgs = BusNotificationDialogFragment.createArgs(
                agency,
                routeTag,
                stopTag,
                new Link("bus", pathParts, new VarTitle(prediction.getTitle()))
            );

            DialogFragment notificationDialog = new BusNotificationDialogFragment();
            notificationDialog.setArguments(notificationDialogArgs);
            notificationDialog.show(fragmentActivity.getSupportFragmentManager(), "notification");
        });

        // Set title
        parentViewHolder.titleTextView.setText(prediction.getTitle());

        // Set prediction minutes
        if (!prediction.getVehiclePredictions().isEmpty()) {
            // Reset title color to default
            parentViewHolder.titleTextView.setTextColor(res.getColor(R.color.black));
            parentViewHolder.directionTextView.setTextColor(res.getColor(R.color.black));

            // Change color of text based on how much time is remaining before the first bus arrives
            int first = prediction.getVehiclePredictions().get(0).getMinutes();

            if (first < 2) {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_soonest));
            } else if (first < 6) {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_soon));
            } else {
                parentViewHolder.minutesTextView.setTextColor(res.getColor(R.color.bus_later));
            }

            parentViewHolder.minutesTextView.setText(formatMinutes(prediction.getVehiclePredictions()));
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
        final List<VehiclePrediction> vehicles = (List<VehiclePrediction>) childListItem;
        childViewHolder.itemView.setOnClickListener(view -> {
            final int adapterPosition = childViewHolder.getAdapterPosition();
            final ParentWrapper item = (ParentWrapper) mItemList.get(adapterPosition - 1);
            collapseParent(item.getParentListItem());
        });
        childViewHolder.aedanButton.setOnClickListener(view -> {
            final int adapterPosition = childViewHolder.getAdapterPosition();
            final ParentWrapper item = (ParentWrapper)  mItemList.get(adapterPosition - 1);
            aedanPublishSubject.onNext((Prediction) item.getParentListItem());
        });
        if (hideAedan) {
            childViewHolder.aedanButton.setVisibility(View.GONE);
        }
        if (!vehicles.isEmpty()) {
            // Set pop-down contents
            childViewHolder.popdownTextView.setText(formatMinutesDetails(vehicles));
            childViewHolder.popdownTextView.setGravity(Gravity.END);
        } else {
            // No predictions loaded
            childViewHolder.popdownTextView.setText("");
        }
    }

    public void updatePredictions(List<? extends Prediction> predictions) {
        final List<? extends ParentListItem> parentItemList = getParentItemList();
        if (parentItemList.size() != predictions.size()) {
            clear();
            addAll(predictions);
        } else {
            for (int i = 0; i < parentItemList.size(); i++) {
                Prediction oldPrediction = (Prediction) parentItemList.get(i);
                Prediction newPrediction = predictions.get(i);

                if (!newPrediction.equals(oldPrediction)) {
                    oldPrediction.setTitle(newPrediction.getTitle());
                    oldPrediction.setTag(newPrediction.getTag());
                    oldPrediction.setDirection(newPrediction.getDirection());
                }

                oldPrediction.setVehiclePredictions(newPrediction.getVehiclePredictions());
                notifyParentItemChanged(i);
            }
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
        ImageView alarmImageView;

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
            alarmImageView = (ImageView) itemView.findViewById(R.id.add_alarm);
        }
    }

    static class PopViewHolder extends ChildViewHolder {
        TextView popdownTextView;
        ImageView aedanButton;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public PopViewHolder(View itemView) {
            super(itemView);
            popdownTextView = (TextView) itemView.findViewById(R.id.popdownTextView);
            aedanButton = (ImageView) itemView.findViewById(R.id.aedan_button);
        }
    }
    
    public PredictionAdapter(FragmentActivity fragmentActivity, List<Prediction> objects, boolean hideAedan) {
        super(objects);
        this.fragmentActivity = fragmentActivity;
        predictions = objects;
        this.hideAedan = hideAedan;
    }

    /**
     * Create "Arriving in..." string, e.g.<br>
     *      Arriving in 2 minutes.<br>
     *         Arriving in 2 and 4 minutes.<br>
     *         Arriving in 2, 3, and 4 minutes.<br>
     *         Arriving in 1 minute.<br>
     * @param predictions Array of arrival times in minutes with vehicle ids
     * @return Formatted arrival time string
     */
    private String formatMinutes(List<VehiclePrediction> predictions) {
        Resources resources = fragmentActivity.getResources();
        StringBuilder result = new StringBuilder(resources.getString(R.string.bus_prediction_begin));
        
        for (int i = 0; i < predictions.size(); i++) {
            if (i != 0 && i == predictions.size() - 1) result.append(resources.getString(R.string.bus_and));
            
            if (predictions.get(i).getMinutes() < 1) result.append(" <1");
            else result.append(" ").append(predictions.get(i).getMinutes());
            
            if (predictions.size() > 2 && i != predictions.size() - 1) result.append(",");
        }
        
        if (predictions.size() == 1 && predictions.get(0).getMinutes() == 1) result.append(resources.getString(R.string.bus_minute_singular));
        else result.append(resources.getString(R.string.bus_minute_plural));
        
        return result.toString();
    }

    /**
     * Create pop-down details string<br/>
     * e.g. <b>10</b> minutes at <b>12:30</b>
     * @param vehicles Array of arrival times in minutes with a vehicle
     * @return Formatted and stylized arrival time details string
     */
    private CharSequence formatMinutesDetails(List<VehiclePrediction> vehicles) {
        Resources resources = fragmentActivity.getResources();
        SpannableStringBuilder result = new SpannableStringBuilder();

        final List<Integer> minutes = new ArrayList<>();
        for (final VehiclePrediction prediction : vehicles) {
            minutes.add(prediction.getMinutes());
        }

        for (int i = 0; i < minutes.size(); i++) {
            // Hack so that it displays "<1 minute" instead of "0 minutes"
            int mins = minutes.get(i) == 0 ? 1 : minutes.get(i);
            String minString = minutes.get(i) < 1 ? "&lt;1" : Integer.toString(mins);

            // Determine bus arrival time
            Date date = new Date();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, minutes.get(i));
            String arrivalTime = android.text.format.DateFormat.is24HourFormat(fragmentActivity) ?
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
