package edu.rutgers.css.Rutgers.auxiliary;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.rutgers.css.Rutgers2.R;

/**
 * Array adapter extended for RSS items
 *
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {
	
	private static final String TAG = "RSSAdapter";
	private int layoutResource;
	
	/* Class to hold data for RSS list rows */
	public final class RSSHolder {
			ImageView iconImageView;
			Bitmap iconImageBitmap;
			TextView titleTextView;
			TextView authordateTextView;
			TextView descriptionTextView;
	}
	
	public RSSAdapter(Context context, int resource, List<RSSItem> objects) {
		super(context, resource, objects);
		this.layoutResource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RSSHolder mHolder = null;
		
		/* Make new data holder or get existing one */
/*		if(convertView == null) {
				mHolder = new RSSHolder();
				// Create new RSS Row view 
				convertView = mLayoutInflater.inflate(this.layoutResource, null);
				convertView.setTag(mHolder);
		}
		else {	
				mHolder = (RSSHolder) convertView.getTag();
		}
*/
		/* Create new RSS Row view */
		if(convertView == null) mHolder = new RSSHolder();
		else mHolder = (RSSHolder) convertView.getTag();
		
		convertView = mLayoutInflater.inflate(this.layoutResource, null);
		convertView.setTag(mHolder);
		
		/* Populate RSS row layout elements */
		mHolder.iconImageView = (ImageView) convertView.findViewById(R.id.rssRowIconView);
		mHolder.titleTextView = (TextView) convertView.findViewById(R.id.rssRowTitleView);
		mHolder.authordateTextView = (TextView) convertView.findViewById(R.id.rssRowAuthorDateView);
		mHolder.descriptionTextView = (TextView) convertView.findViewById(R.id.rssRowDescView);
		
		mHolder.titleTextView.setText(this.getItem(position).getTitle());
		mHolder.authordateTextView.setText(this.getItem(position).getDate());
		mHolder.descriptionTextView.setText(this.getItem(position).getDescription());
		
		/* Download image */
		if(this.getItem(position).getImgUrl() == null) {
			//TODO Don't draw blank ImageView and have it take up space
		}
		else {
			if(mHolder.iconImageView.getTag() == null) {
				new AsyncGetImage(mHolder.iconImageView).execute(this.getItem(position).getImgUrl());
			}
			else {
				Bitmap savedBitmap = (Bitmap) mHolder.iconImageView.getTag();
				mHolder.iconImageView.setImageBitmap(savedBitmap);
			}
		}
		
		return convertView;
	}

}
