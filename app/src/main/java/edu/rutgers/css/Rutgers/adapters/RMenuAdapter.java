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
	private int mItemResource;
	private int mCategoryResource;
    private List<RMenuRow> mData;

	private static enum ViewTypes {
		HEADER, CLICKABLE, UNCLICKABLE;
	}
    private static ViewTypes[] viewTypes = ViewTypes.values();
	
	static class ViewHolder {
		TextView titleTextView;
		ImageView iconImageView;
	}
	
	/**
	 * RMenuAdapter constructor
	 * @param context App context
	 * @param itemResource Layout to use for menu items
	 * @param categoryResource Layout to use for section headers
	 * @param objects List of menu objects to use
	 */
	public RMenuAdapter(Context context, int itemResource, int categoryResource, List<RMenuRow> objects) {
		super(context, itemResource, objects);
		
		this.mItemResource = itemResource;
		this.mCategoryResource = categoryResource;
        this.mData = objects;
	}

    /**
     * Remove item at index.
     * @param index Index of item to remove
     */
    public void remove(int index) {
        if(mData == null) return;
        mData.remove(index);
        notifyDataSetChanged();
    }

    /**
     * Types of row items:
     * 1. Category headers
     * 2. Unclickable items
     * 3. Clickable items
     */
    @Override
    public int getViewTypeCount() {
        return viewTypes.length;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RMenuRow curItem = this.getItem(position);
		ViewHolder holder;
		
		// Choose appropriate layout
		if(convertView == null) {
            switch(viewTypes[getItemViewType(position)]) {
                // Section headers
                case HEADER:
                    convertView = mLayoutInflater.inflate(this.mCategoryResource, null);
                    break;
                // Menu items
                default:
                    convertView = mLayoutInflater.inflate(this.mItemResource, null);
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

}
