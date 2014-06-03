package edu.rutgers.css.Rutgers.auxiliary;

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

}
