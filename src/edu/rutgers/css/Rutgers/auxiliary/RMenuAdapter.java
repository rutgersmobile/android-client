package edu.rutgers.css.Rutgers.auxiliary;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.rutgers.css.Rutgers2.R;

/**
 * Array adapter for menus with items and section headers.
 * Takes items which implement the RMenuPart interface. The text for the item
 * is taken from getTitle(). Whether it's a section header is determined with getIsCategory().
 * If the object is a category, the category resource will be used for its layout.
 * If the object is an item, the item resource will be used for its layout.
 */
public class RMenuAdapter extends ArrayAdapter<RMenuPart> {

	private final static String TAG = "RMenuAdapter";
	private int itemResource;
	private int categoryResource;
	
	static class ViewHolder {
		TextView titleTextView;
	}
	
	/**
	 * 
	 * @param context App context
	 * @param itemResource Layout to use for menu items
	 * @param categoryResource Layout to use for section headers
	 * @param objects List of menu objects to use
	 */
	public RMenuAdapter(Context context, int itemResource, int categoryResource, List<RMenuPart> objects) {
		super(context, itemResource, objects);
		
		this.itemResource = itemResource;
		this.categoryResource = categoryResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RMenuPart curItem = this.getItem(position);
		ViewHolder holder = null;
		
		// Choose appropriate layout
		if(convertView == null) {
			// Section headers
			if(getItemViewType(position) == 1) {
				convertView = mLayoutInflater.inflate(this.categoryResource, null);
				// Make category headers unclickable
				convertView.setEnabled(false);
				convertView.setOnClickListener(null);
			}
			// Menu items
			else {
				convertView = mLayoutInflater.inflate(this.itemResource, null);
			}
			
			holder = new ViewHolder();
			holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		// Set item text
		if(holder.titleTextView != null) holder.titleTextView.setText(curItem.getTitle());
		else Log.e(TAG, "R.id.title not found");
		
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
	    if(getItem(position).getIsCategory())
	    	return 1;
	    else
	    	return 0;
	}

	@Override
	public int getViewTypeCount() {
	    return 2;
	}
	
}
