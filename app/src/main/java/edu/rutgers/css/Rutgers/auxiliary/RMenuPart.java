package edu.rutgers.css.Rutgers.auxiliary;

import android.graphics.drawable.Drawable;

public interface RMenuPart {
	public abstract String getTitle();
	public abstract boolean getIsCategory();
	public abstract boolean getIsClickable();
	public abstract Drawable getDrawable();
    public abstract int getColorResId();
}
