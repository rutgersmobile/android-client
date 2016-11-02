package edu.rutgers.css.Rutgers.model.rmenu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import jp.wasabeef.picasso.transformations.CropTransformation;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Array adapter for menus with items and section headers.
 * Takes items which implement the RMenuPart interface. The text for the item
 * is taken from getTitle(). Whether it's a section header is determined with getIsCategory().
 * If the object is a category, the category resource will be used for its layout.
 * If the object is an item, the item resource will be used for its layout.
 */
public class RMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "RMenuAdapter";

    private int mItemResource;
    private int mCategoryResource;
    private List<RMenuRow> mData;
    private Context context;
    private PublishSubject<RMenuRow> clickSubject = PublishSubject.create();

    protected enum ViewTypes {
        HEADER, CLICKABLE, UNCLICKABLE, IMAGE
    }

    protected static ViewTypes[] viewTypes = ViewTypes.values();
    
    protected static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
        }
    }

    protected static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.title);
        }
    }

    /**
     * RMenuAdapter constructor
     * @param context App context
     * @param itemResource Layout to use for menu items
     * @param categoryResource Layout to use for section headers
     * @param objects List of menu objects to use
     */
    public RMenuAdapter(Context context, int itemResource, int categoryResource, List<RMenuRow> objects) {
        super();

        this.context = context;
        this.mItemResource = itemResource;
        this.mCategoryResource = categoryResource;
        this.mData = objects;
    }

    public RMenuRow getItem(int position) {
        return mData.get(position);
    }

    /**
     * Remove item at index.
     * @param index Index of item to remove
     */
    public void remove(int index) {
        if (mData != null) {
            mData.remove(index);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View itemView;
        switch (viewTypes[viewType]) {
            case HEADER:
                itemView = layoutInflater.inflate(mCategoryResource, parent, false);
                return new HeaderViewHolder(itemView);
            case IMAGE:
                itemView = layoutInflater.inflate(R.layout.row_image, parent, false);
                return new ImageViewHolder(itemView);
            default:
                itemView = layoutInflater.inflate(mItemResource, parent, false);
                return new ItemViewHolder(itemView);
        }
    }

    private void bindTextView(final TextView textView, int position) {
        final RMenuRow rMenuRow = getItem(position);

        textView.setText(rMenuRow.getTitle());

        // Set item text color
        if (rMenuRow.getColorResId() != 0) {
            textView.setTextColor(context.getResources().getColor(rMenuRow.getColorResId()));
        } else {
            textView.setTextColor(textView.getTextColors().getDefaultColor());
        }
    }

    private void bindImageView(final ImageView imageView, int position) {
        final RMenuRow rMenuRow = getItem(position);

        if (rMenuRow.getDrawable() != null) {
            imageView.setImageDrawable(rMenuRow.getDrawable());
            imageView.setVisibility(View.VISIBLE);
        } else if (rMenuRow instanceof RMenuImageRow) {
            // Get image from network
            RMenuImageRow imageRowItem = (RMenuImageRow) rMenuRow;
            Picasso.with(context)
                .load(imageRowItem.getDrawableURL())
                .transform(new CropTransformation(imageRowItem.getWidth(), imageRowItem.getHeight(), CropTransformation.CropType.TOP))
                .into(imageView);
        } else {
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RMenuRow item = getItem(position);
        holder.itemView.setOnClickListener(view -> clickSubject.onNext(item));
        switch (viewTypes[getItemViewType(position)]) {
            case HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                bindTextView(headerViewHolder.titleTextView, position);
                return;
            case IMAGE:
                final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                bindImageView(imageViewHolder.imageView, position);
                return;
            default:
                final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                bindTextView(itemViewHolder.textView, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getIsCategory()) {
            return ViewTypes.HEADER.ordinal();
        } else if (getItem(position).getIsClickable()) {
            return ViewTypes.CLICKABLE.ordinal();
        } else if (getItem(position) instanceof RMenuImageRow) {
            return ViewTypes.IMAGE.ordinal();
        } else {
            return ViewTypes.UNCLICKABLE.ordinal();
        }
    }

    public void addAll(Collection<? extends RMenuRow> rows) {
        mData.addAll(rows);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public Observable<RMenuRow> getPositionClicks() {
        return clickSubject.asObservable();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
