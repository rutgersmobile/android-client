package edu.rutgers.css.Rutgers.auxiliary;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
	
	public final class PredictionHolder {
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
		PredictionHolder holder = null;
		
		/* Make new data holder or get existing one */
		if(convertView == null) {
				holder = new PredictionHolder();
				convertView = mLayoutInflater.inflate(this.layoutResource, null);
				convertView.setTag(holder);
		}
		else {	
				holder = (PredictionHolder) convertView.getTag();
		}
	
		convertView = mLayoutInflater.inflate(this.layoutResource, null);
		convertView.setTag(holder);
		
		/* Populate prediction row layout elements */
		holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
		holder.directionTextView = (TextView) convertView.findViewById(R.id.direction);
		holder.minutesTextView = (TextView) convertView.findViewById(R.id.minutes);
		
		holder.titleTextView.setText(prediction.getTitle());
		holder.minutesTextView.setText(formatMinutes(prediction.getMinutes()));
		
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

	private String formatMinutes(ArrayList<Integer> minutes) {
		StringBuilder result = new StringBuilder("Arriving in ");
		
		for(int i = 0; i < minutes.size(); i++) {
			result.append(minutes.get(i));
			if(i != minutes.size() - 1) result.append(", ");
		}
		result.append(" minutes");
		
		return result.toString();
	}
	
}
