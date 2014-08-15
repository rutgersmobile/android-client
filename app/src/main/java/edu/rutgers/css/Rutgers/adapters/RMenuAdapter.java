package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers2.R;

/**
 * Array adapter for menus with items and section headers.
 * Takes items which implement the RMenuPart interface. The text for the item
 * is taken from getTitle(). Whether it's a section header is determined with getIsCategory().
 * If the object is a category, the category resource will be used for its layout.
 * If the object is an item, the item resource will be used for its layout.
 */
public class RMenuAdapter extends ArrayAdapter<RMenuRow> {

	private final static String TAG = "RMenuAdapter";
	private int itemResource;
	private int categoryResource;

	private static enum ViewTypes {
		HEADER, CLICKABLE, UNCLICKABLE;
	}
	
	static class ViewHolder {
		TextView titleTextView;
		ImageView iconImageView;
	}
	
	/**
	 * 
	 * @param context App context
	 * @param itemResource Layout to use for menu items
	 * @param categoryResource Layout to use for section headers
	 * @param objects List of menu objects to use
	 */
	public RMenuAdapter(Context context, int itemResource, int categoryResource, List<RMenuRow> objects) {
		super(context, itemResource, objects);
		
		this.itemResource = itemResource;
		this.categoryResource = categoryResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RMenuRow curItem = this.getItem(position);
		ViewHolder holder;
		
		// Choose appropriate layout
		if(convertView == null) {
			// Section headers
			if(getItemViewType(position) == ViewTypes.HEADER.ordinal()) {
				convertView = mLayoutInflater.inflate(this.categoryResource, null);
			}
			// Menu items
			else {
				convertView = mLayoutInflater.inflate(this.itemResource, null);
			}

			holder = new ViewHolder();
			holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
			holder.iconImageView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		// Set item text
		if(holder.titleTextView != null) holder.titleTextView.setText(curItem.getTitle());
		else Log.e(TAG, "R.id.title not found");

        // Set item text color
        if(curItem.getColorResId() != 0) {
            holder.titleTextView.setTextColor(getContext().getResources().getColor(curItem.getColorResId()));
        }
        else {
            holder.titleTextView.setTextColor(holder.titleTextView.getTextColors().getDefaultColor());
        }
		
		// Set icon
		if(holder.iconImageView != null){
			if(curItem.getDrawable() != null) {
				holder.iconImageView.setImageDrawable(curItem.getDrawable());
				holder.iconImageView.setVisibility(View.VISIBLE);
			}
			else {
				holder.iconImageView.setVisibility(View.GONE);
			}
		}

		return convertView;
	}
	
	/**
	 * Types of row items:
	 * 1. Category headers
	 * 2. Unclickable items
	 * 3. Clickable items
	 */
	@Override
	public int getViewTypeCount() {
	    return ViewTypes.values().length;
	}
	
	@Override
	public int getItemViewType(int position) {
	    if(getItem(position).getIsCategory()) return ViewTypes.HEADER.ordinal();
	    else if(getItem(position).getIsClickable()) return ViewTypes.CLICKABLE.ordinal();
	    else return ViewTypes.UNCLICKABLE.ordinal();
	}

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        RMenuRow curItem = this.getItem(position);
        if(curItem == null) return false;
        else if(!curItem.getIsClickable()) return false;
        else return true;
    }

}
