package edu.rutgers.css.Rutgers.items.RMenu;

import android.graphics.drawable.Drawable;

public abstract class RMenuRow {

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public abstract boolean getIsCategory();

    public abstract boolean getIsClickable();

    public Drawable getDrawable() {
        return null;
    }

    public int getColorResId() {
        return 0;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
