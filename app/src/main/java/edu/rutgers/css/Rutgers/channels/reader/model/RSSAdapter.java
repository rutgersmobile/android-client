package edu.rutgers.css.Rutgers.channels.reader.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import jp.wasabeef.picasso.transformations.CropTransformation;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Array adapter extended for RSS items
 *
 */
public class RSSAdapter extends RecyclerView.Adapter<RSSAdapter.ViewHolder> {
    
    private static final String TAG = "RSSAdapter";
    private int mLayoutResource;
    private final int mTargetWidth;
    private final List<RSSItem> items;
    private final Context context;
    private final PublishSubject<RSSItem> itemPublishSubject = PublishSubject.create();

    public void addAll(Collection<? extends RSSItem> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public Observable<RSSItem> getPositionClicks() {
        return itemPublishSubject.asObservable();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(mLayoutResource, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RSSItem curItem = this.getItem(position);
        holder.itemView.setOnClickListener(view -> itemPublishSubject.onNext(curItem));

        // Populate RSS row layout elements
        holder.titleTextView.setText(curItem.getTitle());

        if (StringUtils.isNotBlank(curItem.getDate())) {
            holder.dateTextView.setText(curItem.getDate());
            holder.dateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.dateTextView.setVisibility(View.GONE);
            holder.dateTextView.setText(null);
        }

        holder.descriptionTextView.setText(curItem.getDescription());
        //holder.authorTextView.setText(curItem.getAuthor());

        // Download image
        if (curItem.getImgUrl() == null) {
            // No image - clear the image view
            holder.iconImageView.setImageBitmap(null);
            holder.iconImageView.setVisibility(View.GONE);
        } else {
            holder.iconImageView.setVisibility(View.VISIBLE);
            // Download the image
            final String imageURL = curItem.getImgUrl().toString();
            final String imageExtension = imageURL.substring(imageURL.lastIndexOf("."));
            if (!imageExtension.equals(".gif")) {
                Picasso.with(context)
                    .load(imageURL)
                    .transform(new CropTransformation(mTargetWidth, mTargetWidth, CropTransformation.CropType.TOP))
                    .into(holder.iconImageView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public RSSItem getItem(int position) {
        return items.get(position);
    }

    // Class to hold data for RSS list rows
    public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iconImageView;
            TextView titleTextView;
            TextView dateTextView;
            TextView descriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.iconImageView = (ImageView) itemView.findViewById(R.id.rssRowIconView);
            this.titleTextView = (TextView) itemView.findViewById(R.id.rssRowTitleView);
            this.dateTextView = (TextView) itemView.findViewById(R.id.rssRowDateView);
            this.descriptionTextView = (TextView) itemView.findViewById(R.id.rssRowDescView);
        }
    }

    public RSSAdapter(Context context, int resource, List<RSSItem> items) {
        super();
        this.mLayoutResource = resource;
        this.items = items;
        this.context = context;
        this.mTargetWidth = (int) context.getResources().getDimension(R.dimen.rss_image_width);
    }
}
