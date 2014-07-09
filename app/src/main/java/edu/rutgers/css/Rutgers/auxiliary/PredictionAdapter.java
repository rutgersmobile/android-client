package edu.rutgers.css.Rutgers.auxiliary;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.fragments.BusDisplay;
import edu.rutgers.css.Rutgers2.R;

/**
 * Bus Prediction object adapter
 * Populates bus arrival time rows for the bus channel.
 */
public class PredictionAdapter extends ArrayAdapter<Prediction> {

	private static final String TAG = "PredictionAdapter";
    private static final SimpleDateFormat arriveDf = new SimpleDateFormat("h:mm a", Locale.US);

	private int layoutResource;
    private ArrayList<Integer> poppedRows;
	
	static class ViewHolder {
		TextView titleTextView;
		TextView directionTextView;
		TextView minutesTextView;
        TextView popdownTextView;
        LinearLayout popdownLayout;
	}
	
	public PredictionAdapter(Context context, int resource, List<Prediction> objects) {
		super(context, resource, objects);
		layoutResource = resource;
        poppedRows = new ArrayList<Integer>();
	}

    /**
     * Toggle pop-down on a row.
     * @param position Position of the row to toggle
     */
    public void togglePopped(int position) {
        if(!poppedRows.remove(new Integer(position))) {
            poppedRows.add(new Integer(position));
        }
        notifyDataSetChanged();
    }

    /**
     * Check if row's pop-down is open.
     * @param position Position of the row to check
     * @return True if the row's pop-down is open, false if not.
     */
    public boolean isPopped(int position) {
        return poppedRows.contains(new Integer(position));
    }

    public void saveState(Bundle outState) {
        outState.putSerializable("paPoppedRows", poppedRows);
    }

    public void restoreState(Bundle inState) {
        if(inState.getSerializable("paPoppedRows") != null) {
            poppedRows = (ArrayList<Integer>) inState.getSerializable("paPoppedRows");
        }
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Prediction prediction = this.getItem(position);
        Resources res = getContext().getResources();
		ViewHolder holder = null;
		
		// Make new data holder or get existing one
		if(convertView == null) {
			convertView = layoutInflater.inflate(this.layoutResource, null);
			holder = new ViewHolder();
			holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
			holder.directionTextView = (TextView) convertView.findViewById(R.id.direction);
			holder.minutesTextView = (TextView) convertView.findViewById(R.id.minutes);
            holder.popdownLayout = (LinearLayout) convertView.findViewById(R.id.popdownLayout);
            holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
			convertView.setTag(holder);
		}
		else {	
			holder = (ViewHolder) convertView.getTag();
		}

		// Set title
		holder.titleTextView.setText(prediction.getTitle());

        // Set prediction minutes
		if(!prediction.getMinutes().isEmpty()) {
            // Reset title color to default
            holder.titleTextView.setTextColor(res.getColor(R.color.black));
            holder.directionTextView.setTextColor(res.getColor(R.color.black));

            // Change color of text based on how much time is remaining before the first bus arrives
			int first = prediction.getMinutes().get(0);

			if(first < 2) {
				holder.minutesTextView.setTextColor(res.getColor(R.color.bus_soonest));
			}
			else if(first < 6) {
				holder.minutesTextView.setTextColor(res.getColor(R.color.bus_soon));
			}
			else {
				holder.minutesTextView.setTextColor(res.getColor(R.color.bus_later));
			}

			holder.minutesTextView.setText(formatMinutes(prediction.getMinutes()));

            // Set pop-down contents
            holder.popdownTextView.setText(formatMinutesDetails(prediction.getMinutes()));
            holder.popdownTextView.setGravity(Gravity.RIGHT);
		}
		else {
			// No predictions loaded - gray out all text
            holder.titleTextView.setTextColor(res.getColor(R.color.light_gray));
            holder.minutesTextView.setTextColor(res.getColor(R.color.light_gray));
			holder.minutesTextView.setText(R.string.bus_no_predictions);
            holder.popdownTextView.setText("");
		}

        // Set direction
		if(prediction.getDirection() == null || prediction.getDirection().isEmpty()) {
            // If there's no direction string, don't let that text field take up space
			holder.directionTextView.setVisibility(View.GONE);
		}
		else {
            holder.directionTextView.setText(prediction.getDirection());
			holder.directionTextView.setVisibility(View.VISIBLE);
		}

        // Show pop-down if it is opened on this row (and there are minutes available)
        if(!prediction.getMinutes().isEmpty() && isPopped(position)) {
            holder.popdownLayout.setVisibility(View.VISIBLE);
        }
        else {
            // Toggle pop-down off if prediction updated with no data available
            if(isPopped(position)) togglePopped(position);

            holder.popdownLayout.setVisibility(View.GONE);
        }

		return convertView;
	}

	/**
	 * Create "Arriving in..." string
	 * e.g. Arriving in 2 minutes.
	 * 		Arriving in 2 and 4 minutes.
	 * 		Arriving in 2, 3, and 4 minutes.
	 * 		Arriving in 1 minute.
	 * @param minutes Array of arrival times in minutes
	 * @return Formatted arrival time string
	 */
	private String formatMinutes(ArrayList<Integer> minutes) {
		Resources resources = getContext().getResources();
		StringBuilder result = new StringBuilder(resources.getString(R.string.bus_prediction_begin));
		
		for(int i = 0; i < minutes.size(); i++) {
			if(i != 0 && i == minutes.size() - 1) result.append(resources.getString(R.string.bus_and));
			
			if(minutes.get(i) < 1) result.append(" <1");
			else result.append(" "+minutes.get(i));
			
			if(minutes.size() > 2 && i != minutes.size() - 1) result.append(",");
		}
		
		if(minutes.size() == 1 && minutes.get(0) == 1) result.append(resources.getString(R.string.bus_minute_singular));
		else result.append(resources.getString(R.string.bus_minute_plural));
		
		return result.toString();
	}

    /**
     * Create pop-down details string<br/>
     * e.g. <b>10</b> minutes at <b>12:30</b>
     * @param minutes Array of arrival times in minutes
     * @return Formatted and stylized arrival time details string
     */
    private CharSequence formatMinutesDetails(ArrayList<Integer> minutes) {
        Resources resources = getContext().getResources();
        SpannableStringBuilder result = new SpannableStringBuilder();

        for(int i = 0; i < minutes.size(); i++) {
            // Hack so that it displays "<1 minute" instead of "0 minutes"
            int mins = minutes.get(i) == 0 ? 1 : minutes.get(i);
            String minString = minutes.get(i) < 1 ? "&lt;1" : new Integer(mins).toString();

            // Determine bus arrival time
            Date date = new Date();
            Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(date);
            cal.add(Calendar.MINUTE, minutes.get(i));
            String arrivalTime = PredictionAdapter.arriveDf.format(cal.getTime());

            // Get appropriate string & format, stylize
            String line = resources.getQuantityString(R.plurals.bus_minute_details, mins, minString, arrivalTime);
            CharSequence styledLine = Html.fromHtml(line);
            result.append(styledLine);
            if(i != minutes.size() - 1) result.append("\n");
        }

        return result;
    }
	
}
