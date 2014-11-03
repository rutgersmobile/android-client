package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

import edu.rutgers.css.Rutgers.items.RSSItem;
import edu.rutgers.css.Rutgers2.R;

/**
 * Array adapter extended for RSS items
 *
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {
    
    private static final String TAG = "RSSAdapter";
    private int mLayoutResource;
    private int mTargetWidth;
    private AQuery aq;
    
    // Class to hold data for RSS list rows
    static class ViewHolder {
            ImageView iconImageView;
            TextView titleTextView;
            TextView dateTextView;
            TextView descriptionTextView;
            //TextView authorTextView;
    }
    
    public RSSAdapter(Context context, int resource, List<RSSItem> objects) {
        super(context, resource, objects);
        this.mLayoutResource = resource;
        this.aq = new AQuery(context);
        
        this.mTargetWidth = (int) getContext().getResources().getDimension(R.dimen.rss_image_width);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        RSSItem curItem = this.getItem(position);
        if(curItem == null) return false;
        else if(curItem.getLink() == null) return false;
        else return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;

        if(convertView == null) { 
            convertView = mLayoutInflater.inflate(this.mLayoutResource, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.rssRowIconView);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.rssRowTitleView);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.rssRowDateView);
            holder.descriptionTextView = (TextView) convertView.findViewById(R.id.rssRowDescView);
            //holder.authorTextView = (TextView) convertView.findViewById(R.id.rssRowAuthorView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
    
        RSSItem curItem = this.getItem(position);
        
        // Populate RSS row layout elements
        holder.titleTextView.setText(curItem.getTitle());

        if(curItem.getDate() != null && !curItem.getDate().isEmpty()) {
            holder.dateTextView.setText(curItem.getDate());
            holder.dateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.dateTextView.setVisibility(View.GONE);
            holder.dateTextView.setText(null);
        }

        holder.descriptionTextView.setText(curItem.getDescription());
        //holder.authorTextView.setText(curItem.getAuthor());
        
        // Download image
        if(curItem.getImgUrl() == null) {
            // No image - clear the image view
            holder.iconImageView.setImageBitmap(null);
            holder.iconImageView.setVisibility(View.GONE);
        } else {
            holder.iconImageView.setVisibility(View.VISIBLE);
            // Download the image
            AQuery cvAq = aq.recycle(convertView);
            cvAq.id(holder.iconImageView).image(curItem.getImgUrl().toString(), true, true, mTargetWidth, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
        }
        
        return convertView;
    }

}
