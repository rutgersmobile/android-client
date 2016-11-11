package edu.rutgers.css.Rutgers.ui;

/**
 * Created by mattro on 11/10/16.
 */
public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
