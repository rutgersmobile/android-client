package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.ItemTouchHelperAdapter;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Adapter for bookmarks
 */
public final class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    // User created bookmark
    private final int removableResource;

    // Official Rutgers (TM) bookmark
    private final int lockedResource;

    // Divider between enabled / disabled sections
    private final int dividerResource;

    // used to get SharedPreferences to store links
    private final Context context;

    // Links in the drawer + hidden items
    private final List<Link> links;

    private enum ViewTypes {
        REMOVABLE, LOCKED, DIVIDER
    }
    private static final ViewTypes[] viewTypes = ViewTypes.values();

    public void enableAll() {
        for (final Link link : links) {
            link.setEnabled(true);
        }
        notifyDataSetChanged();
    }

    public void disableAll() {
        for (final Link link : links) {
            link.setEnabled(false);
        }
        notifyDataSetChanged();
    }

    /**
     * This function controls what happens when you drag one item over another.
     * @param positionOne The position that gets dragged to
     * @param positionTwo The position being dragged from
     */
    @Override
    public boolean onItemMove(int positionOne, int positionTwo) {
        final int dividerPosition = dividerPosition();
        if (positionOne < dividerPosition && positionTwo < dividerPosition) {
            // They are both enabled so just swap them
            swapAll(links, positionOne, positionTwo);
            notifyItemMoved(positionOne, positionTwo);
            return true;
        } else if (positionOne > dividerPosition && positionTwo > dividerPosition) {
            // They are both disabled. Just swap them, but remember to subtract for the divider
            swapAll(links, positionOne - 1, positionTwo - 1);
            notifyItemMoved(positionOne, positionTwo);
            return true;
        } else if (positionOne < dividerPosition && positionTwo == dividerPosition) {
            // We're dragging over the divider from above
            // Don't do anything to user added links since they can only be deleted
            final Link link = links.get(positionOne);
            if (!link.isRemovable()) {
                link.setEnabled(false);
                notifyItemMoved(positionOne, positionTwo);
                return true;
            }

            // If we made it here then we're not actually swapping anything
            // This is the case when we try to drag a user link over the divider
            return false;
        } else if (positionOne > dividerPosition && positionTwo == dividerPosition) {
            // We're dragging over the divider from below. Just enable it
            // subtract one from the position to account for the divider
            links.get(positionOne - 1).setEnabled(true);
            notifyItemMoved(positionOne, positionTwo);
            return true;
        }
        return false;
    }

    private static void swapAll(List list, int positionOne, int positionTwo) {
        if (positionOne < positionTwo) {
            for (int i = positionOne; i < positionTwo; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = positionOne; i > positionTwo; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
    }

    @Override
    public void onItemDismiss(int position) {
        final Link link = getItem(position);
        if (link == null) {
            return;
        }

        if (link.isRemovable()) {
            remove(position);
        } else {
            uncheck(link, position, false);
        }
    }

    // This class will keep references to the view so we don't
    // have to look them up every time we get a view we've already inflated
    public final class RemovableViewHolder extends RecyclerView.ViewHolder{
        ImageView imageButton;
        TextView textView;
        ImageView imageView;

        public RemovableViewHolder(View itemView) {
            super(itemView);
            imageButton = (ImageView) itemView.findViewById(R.id.delete_bookmark);
            textView = (TextView) itemView.findViewById(R.id.title);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

        public ImageView getImageButton() {
            return imageButton;
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public final class LockedViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;
        ImageView imageView;

        public LockedViewHolder(View itemView) {
            super(itemView);

            checkBox = (CheckBox) itemView.findViewById(R.id.toggle_bookmark);
            textView = (TextView) itemView.findViewById(R.id.title);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    private final class DividerViewHolder extends RecyclerView.ViewHolder {
        public DividerViewHolder(View itemView) {
            super(itemView);
        }
    }

    // Java doesn't have tuples so this is what you have to do if
    // you want to return multiple values
    private final class TwoLists {
        private final List<Link> visible;
        private final List<Link> hidden;

        public TwoLists(final List<Link> visible, final List<Link> hidden) {
            this.visible = visible;
            this.hidden = hidden;
        }

        public List<Link> getVisible() {
            return visible;
        }

        public List<Link> getHidden() {
            return hidden;
        }
    }

    // This function will split a list of links into a list of visible links and a list of hidden links
    // We use this to make sure the links are in the right order when we load them
    private TwoLists splitVisible(List<Link> links) {
        final List<Link> visible = new ArrayList<>();
        final List<Link> hidden = new ArrayList<>();
        for (final Link link : links) {
            if (link.isEnabled()) {
                visible.add(link);
            } else {
                hidden.add(link);
            }
        }

        return new TwoLists(visible, hidden);
    }

    // Find the first disabled link
    // Everything after the divider is disabled
    private int dividerPosition() {
        for (int i = 0; i < links.size(); i++) {
            if (!links.get(i).isEnabled()) {
                return i;
            }
        }

        return links.size();
    }

    // Initialize the adapter with bookmarks saved in the preferences
    public void addFromPrefs() {
        final List<Link> links = PrefUtils.getBookmarks(context);
        final TwoLists lists = splitVisible(links);
        this.links.addAll(lists.getVisible());
        this.links.addAll(lists.getHidden());
    }

    public BookmarkAdapter(final Context context, int removableResource, int lockedResource, int dividerResource) {
        this.context = context;
        this.removableResource = removableResource;
        this.lockedResource = lockedResource;
        this.dividerResource = dividerResource;
        this.links = new ArrayList<>();

        // Update persistent storage when the list is updated
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                PrefUtils.setBookmarks(context, links);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewTypes[viewType]) {
            case REMOVABLE:
                final View removeableItemView = inflater.inflate(removableResource, parent, false);
                return new RemovableViewHolder(removeableItemView);
            case LOCKED:
                final View lockedItemView = inflater.inflate(lockedResource, parent, false);
                return new LockedViewHolder(lockedItemView);
            default:
                final View dividerItemView = inflater.inflate(dividerResource, parent, false);
                return new DividerViewHolder(dividerItemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(viewTypes[getItemViewType(position)]) {
            case REMOVABLE:
                onBindRemovableViewHolder((RemovableViewHolder) holder, position);
                return;
            case LOCKED:
                onBindLockedViewHolder((LockedViewHolder) holder, position);
        }
    }

    private void onBindRemovableViewHolder(RemovableViewHolder holder, int position) {
        final Link link = getItem(position);

        holder.textView.setText(link.getTitle());
        holder.imageView.setImageDrawable(getIconDrawableForLink(link));
        holder.imageButton.setOnClickListener(v -> remove(position));
    }

    private void onBindLockedViewHolder(LockedViewHolder holder, int position) {
        final Link link = getItem(position);

        holder.textView.setText(link.getTitle());
        holder.imageView.setImageDrawable(getIconDrawableForLink(link));

        holder.checkBox.setChecked(link.isEnabled());
        holder.checkBox.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            uncheck(link, position, checkBox.isChecked());
        });
    }

    /**
     * We need to move the link between sections so we just insert it at the divider position
     * if it was enabled it will go to the end of the enabled section
     * otherwise it goes to the beginning of the disabled section
     * This is due to the divider position being entirely defined by where
     * the first disabled item is
     */
    private void uncheck(Link link, int position, boolean isChecked) {
        removeNoNotify(position);
        link.setEnabled(isChecked);
        links.add(dividerPosition(), link);
        notifyDataSetChanged();
    }

    private int getIconForLink(Link link) {
        return context.getResources().getIdentifier("ic_" + link.getHandle(), "drawable", Config.PACKAGE_NAME);
    }

    private Drawable getIconDrawableForLink(Link link) {
        return ImageUtils.getIcon(
            context.getResources(),
            getIconForLink(link),
            R.color.dark_gray
        );
    }

    @Override
    public int getItemViewType(int position) {
        final Link link = getItem(position);

        if (link == null) {
            return ViewTypes.DIVIDER.ordinal();
        }

        if (link.isRemovable()) {
            return ViewTypes.REMOVABLE.ordinal();
        }

        return ViewTypes.LOCKED.ordinal();
    }

    @Override
    public int getItemCount() {
        return links.size() + 1;
    }

    public Link getItem(int position) {
        final int dividerPosition = dividerPosition();
        if (position == dividerPosition) {
            return null;
        }

        if (position < dividerPosition) {
            return links.get(position);
        }

        return links.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        final Link link = getItem(position);

        if (link == null) {
            return 0;
        }

        return link.hashCode();
    }

    private void removeNoNotify(int position) {
        final int dividerPosition = dividerPosition();
        if (position < dividerPosition) {
            links.remove(position);
        } else if (position > dividerPosition) {
            links.remove(position - 1);
        }
    }

    public void remove(int position) {
        removeNoNotify(position);
        notifyDataSetChanged();
    }
}
