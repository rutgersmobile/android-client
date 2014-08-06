package edu.rutgers.css.Rutgers.items;

import android.graphics.drawable.Drawable;

public class RMenuHeaderRow implements RMenuRow {
	
	private String title;
	
	public RMenuHeaderRow(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean getIsCategory() {
		return true;
	}

	@Override
	public boolean getIsClickable() {
		return false;
	}
	
	@Override
	public Drawable getDrawable() {
		return null;
	}

    @Override
    public int getColorResId() {
        return 0;
    }
	
}
