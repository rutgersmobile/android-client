package edu.rutgers.css.Rutgers.auxiliary;

import android.os.Bundle;

/**
 * Helper class for passing arguments to Component Factory. Fields include:
 * 	title		(required)
 * 	component	(required)
 * 	url			(optional)
 * 	data		(optional JSON object or array in string representation)
 */
public class SlideMenuItem implements RMenuPart {

	private Bundle args;
	private boolean clickable;
	
	/**
	 * Initialize menu item with a custom argument bundle.
	 * @param args Custom argument bundle, which needs at least the title and component fields.
	 */
	public SlideMenuItem(Bundle args) {
		this.setArgs(args);
		this.setClickable(true);
	}
	
	/**
	 * Initialize menu item with a channel title and component ID.
	 * @param title Title of the channel/item
	 * @param component Component identifier
	 */
	public SlideMenuItem(String title, String component) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("component", component);
		this.setArgs(args);
		this.setClickable(true);
	}
	
	/**
	 * Initialize menu item with channel title, component ID, and a URL argument.
	 * @param title Title of the channel/item
	 * @param component Component identifier
	 * @param url URL argument passed to component
	 */
	public SlideMenuItem(String title, String component, String url) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("component", component);
		args.putString("url", url);
		this.setArgs(args);
		this.setClickable(true);
	}
	
	/**
	 * Create an unclickable menu item with text
	 * @param title Item text
	 */
	public SlideMenuItem(String title) {
		Bundle args = new Bundle();
		args.putString("title", title);
		this.setArgs(args);
		this.setClickable(false);
	}
	
	public void setClickable(boolean b) {
		this.clickable = b;
	}
	
	@Override
	public boolean getClickable() {
		return this.clickable;
	}

	public void setArgs(Bundle args) {
		this.args = args;
	}
	
	public Bundle getArgs() {
		return this.args;
	}
	
	public String toString() {
		return args.getString("title");
	}

	@Override
	public String getTitle() {
		return this.args.getString("title");
	}

	@Override
	public boolean getIsCategory() {
		return false;
	}
	
}
