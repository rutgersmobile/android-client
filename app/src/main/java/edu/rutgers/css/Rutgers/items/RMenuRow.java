package edu.rutgers.css.Rutgers.items;

import android.graphics.drawable.Drawable;

public interface RMenuRow {
	public abstract String getTitle();
	public abstract boolean getIsCategory();
	public abstract boolean getIsClickable();
	public abstract Drawable getDrawable();
    public abstract int getColorResId();
}
