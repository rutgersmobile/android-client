package edu.rutgers.css.Rutgers.auxiliary;

import android.graphics.drawable.Drawable;

public class SlideMenuHeader implements RMenuPart {
	
	private String title;
	
	public SlideMenuHeader(String title) {
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
