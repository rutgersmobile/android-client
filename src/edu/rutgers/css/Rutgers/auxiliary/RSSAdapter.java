package edu.rutgers.css.Rutgers.auxiliary;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.rutgers.css.Rutgers.R;

/**
 * Array adapter extended for RSS items
 *
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {

	private int layoutResource;
	
	/* Class to hold data for RSS list rows */
	private final class RSSHolder {
			ImageView iconImageView;
			TextView titleTextView;
			TextView authordateTextView;
			TextView descriptionTextView;
	}
	
	public RSSAdapter(Context context, int resource, List<RSSItem> objects) {
		super(context, resource, objects);
		this.layoutResource = resource;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RSSHolder mHolder = null;
		
		/* Make new data holder or get existing one */
		if(convertView == null) {
				mHolder = new RSSHolder();
				/* Create new RSS Row view */
				convertView = mLayoutInflater.inflate(this.layoutResource, null);
				convertView.setTag(mHolder);
		}
		else {	
				mHolder = (RSSHolder) convertView.getTag();
		}

		/* Populate RSS row layout elements */
		mHolder.iconImageView = (ImageView) convertView.findViewById(R.id.rssRowIconView);
		mHolder.titleTextView = (TextView) convertView.findViewById(R.id.rssRowTitleView);
		mHolder.authordateTextView = (TextView) convertView.findViewById(R.id.rssRowAuthorDateView);
		mHolder.descriptionTextView = (TextView) convertView.findViewById(R.id.rssRowDescView);
		
		mHolder.titleTextView.setText(this.getItem(position).title);
		mHolder.authordateTextView.setText(this.getItem(position).date);
		mHolder.descriptionTextView.setText(this.getItem(position).description);
		
		return convertView;
	}
	
}
