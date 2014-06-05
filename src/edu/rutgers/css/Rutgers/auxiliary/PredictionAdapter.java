package edu.rutgers.css.Rutgers.auxiliary;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.rutgers.css.Rutgers2.R;

/**
 * Bus Prediction object adapter
 * Populates bus arrival time rows for the bus channel.
 */
public class PredictionAdapter extends ArrayAdapter<Prediction> {

	private static final String TAG = "PredictionAdapter";
	private int layoutResource;
	
	static class ViewHolder {
		TextView titleTextView;
		TextView directionTextView;
		TextView minutesTextView;
	}
	
	public PredictionAdapter(Context context, int resource, List<Prediction> objects) {
		super(context, resource, objects);
		this.layoutResource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Prediction prediction = this.getItem(position);
		ViewHolder holder = null;
		
		/* Make new data holder or get existing one */
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(this.layoutResource, null);
			holder = new ViewHolder();
			holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
			holder.directionTextView = (TextView) convertView.findViewById(R.id.direction);
			holder.minutesTextView = (TextView) convertView.findViewById(R.id.minutes);
			convertView.setTag(holder);
		}
		else {	
			holder = (ViewHolder) convertView.getTag();
		}
	
		/* Populate prediction row layout elements */
		holder.titleTextView.setText(prediction.getTitle());
		
		/* Change color of text based on how much time is remaining before the first bus arrives */
		if(prediction.getMinutes().size() > 0) {
			int first = prediction.getMinutes().get(0);
			if(first < 2) {
				holder.minutesTextView.setTextColor(getContext().getResources().getColor(R.color.busSoonest));
			}
			else if(first < 6) {
				holder.minutesTextView.setTextColor(getContext().getResources().getColor(R.color.busSoon));
			}
			else {
				holder.minutesTextView.setTextColor(getContext().getResources().getColor(R.color.busLater));
			}
			
			holder.minutesTextView.setText(formatMinutes(prediction.getMinutes()));
		}
		else {
			// No predictions loaded
			holder.minutesTextView.setText(R.string.bus_no_predictions);
		}
		
		/* If there's no direction string, don't let that text field take up space */
		if(prediction.getDirection() == null || prediction.getDirection().equals("")) {
			holder.directionTextView.setVisibility(View.INVISIBLE);
			holder.directionTextView.setMaxLines(0);
		}
		else {
			holder.directionTextView.setVisibility(View.VISIBLE);
			holder.directionTextView.setMaxLines(1);
			holder.directionTextView.setText(prediction.getDirection());
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
	
}
