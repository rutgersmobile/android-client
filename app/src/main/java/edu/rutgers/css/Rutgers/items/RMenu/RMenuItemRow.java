package edu.rutgers.css.Rutgers.items.RMenu;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

/**
 * Helper class for passing arguments to Component Factory. Fields include:
 *     title        (required for displaying in list)
 *     component    (required when passed to component factory)
 *     url            (optional)
 *     data        (optional JSON object or array in string representation)
 */
public class RMenuItemRow extends RMenuRow {

    private Bundle args;
    private boolean clickable;
    private Drawable drawable;
    private int colorResId;
    
    /**
     * Initialize menu item with a custom argument bundle.
     * @param args Custom argument bundle, which needs at least the title and component fields.
     */
    public RMenuItemRow(Bundle args) {
        setArgs(args);
        setClickable(true);
        setColorResId(args.getInt("color"));
    }
    
    /**
     * Create an unclickable menu item with text
     * @param title Item text
     */
    public RMenuItemRow(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);
        setArgs(args);
        setClickable(false);
        setColorResId(args.getInt("color"));
    }

    @Override
    public String getTitle() {
        return this.args.getString("title");
    }

    @Override
    public boolean getIsCategory() {
        return false;
    }

    @Override
    public boolean getIsClickable() {
        return this.clickable;
    }

    public void setClickable(boolean b) {
        this.clickable = b;
    }

    public void setArgs(Bundle args) {
        this.args = args;
    }
    
    public Bundle getArgs() {
        return this.args;
    }
    
    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    
    public Drawable getDrawable() {
        return this.drawable;
    }

    public void setColorResId(int resId) {
        this.colorResId = resId;
    }

    public int getColorResId() {
        return this.colorResId;
    }

}
