package edu.rutgers.css.Rutgers.items;

import android.graphics.drawable.Drawable;

public interface RMenuRow {
	public String getTitle();
	public boolean getIsCategory();
	public boolean getIsClickable();
	public Drawable getDrawable();
    public int getColorResId();
}
