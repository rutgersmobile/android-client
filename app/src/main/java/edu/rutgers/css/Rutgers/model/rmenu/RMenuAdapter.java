package edu.rutgers.css.Rutgers.model.rmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

import edu.rutgers.css.Rutgers.R;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

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
    private AQuery aq;

    protected static enum ViewTypes {
        HEADER, CLICKABLE, UNCLICKABLE, IMAGE
    }
    protected static ViewTypes[] viewTypes = ViewTypes.values();
    
    protected static class ViewHolder {
        TextView titleTextView;
        ImageView imageView;
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
        this.aq = new AQuery(context);
    }

    /**
     * Remove item at index.
     * @param index Index of item to remove
     */
    public void remove(int index) {
        if (mData == null) return;
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
        if (getItem(position).getIsCategory()) return ViewTypes.HEADER.ordinal();
        else if (getItem(position).getIsClickable()) return ViewTypes.CLICKABLE.ordinal();
        else if (getItem(position) instanceof RMenuImageRow) return ViewTypes.IMAGE.ordinal();
        else return ViewTypes.UNCLICKABLE.ordinal();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        RMenuRow curItem = this.getItem(position);
        return curItem != null && curItem.getIsClickable();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RMenuRow curItem = this.getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            // Choose appropriate layout
            switch(viewTypes[getItemViewType(position)]) {
                // Section headers
                case HEADER:
                    convertView = mLayoutInflater.inflate(mCategoryResource, null);
                    break;
                case IMAGE:
                    convertView = mLayoutInflater.inflate(R.layout.row_image, null);
                    break;
                // Menu items
                default:
                    convertView = mLayoutInflater.inflate(mItemResource, null);
            }

            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // Set item text
        if (holder.titleTextView != null) {
            holder.titleTextView.setText(curItem.getTitle());

            // Set item text color
            if (curItem.getColorResId() != 0) {
                holder.titleTextView.setTextColor(getContext().getResources().getColor(curItem.getColorResId()));
            } else {
                holder.titleTextView.setTextColor(holder.titleTextView.getTextColors().getDefaultColor());
            }
        } else if (viewTypes[getItemViewType(position)] != ViewTypes.IMAGE) {
            LOGE(TAG, "R.id.title not found for view at position " + position);
        }
        
        // Set image
        if (holder.imageView != null){
            if (curItem.getDrawable() != null) {
                holder.imageView.setImageDrawable(curItem.getDrawable());
                holder.imageView.setVisibility(View.VISIBLE);
            } else if (curItem instanceof RMenuImageRow) {
                // Get image from network
                RMenuImageRow imageRowItem = (RMenuImageRow) curItem;
                AQuery cvAq = aq.recycle(convertView);
                cvAq.id(holder.imageView).image(imageRowItem.getDrawableURL(), false, true, imageRowItem.getWidth(), 0, null, AQuery.FADE_IN_NETWORK);
            } else {
                holder.imageView.setImageBitmap(null);
                holder.imageView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

}
