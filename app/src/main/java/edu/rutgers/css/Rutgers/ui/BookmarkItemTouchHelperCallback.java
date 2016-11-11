package edu.rutgers.css.Rutgers.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.BookmarkAdapter;

/**
 * Drag and drop helper for {@link edu.rutgers.css.Rutgers.ui.fragments.BookmarksDisplay}
 */
public class BookmarkItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private ItemTouchHelperAdapter adapter;

    public BookmarkItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        BookmarkAdapter adapter = (BookmarkAdapter)recyclerView.getAdapter();
        Link link = adapter.getItem(viewHolder.getAdapterPosition());
        if (viewHolder instanceof BookmarkAdapter.LockedViewHolder && link != null) {
            BookmarkAdapter.LockedViewHolder lockedViewHolder = (BookmarkAdapter.LockedViewHolder) viewHolder;
            lockedViewHolder.getCheckBox().setChecked(link.isEnabled());
        }
        adapter.notifyDataSetChanged();
    }
}
