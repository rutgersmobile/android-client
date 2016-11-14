package edu.rutgers.css.Rutgers.ui;

import android.support.v7.widget.RecyclerView;

/**
 * Used by bookmarks for rearranging
 */

public interface OnStartDragListener {
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
