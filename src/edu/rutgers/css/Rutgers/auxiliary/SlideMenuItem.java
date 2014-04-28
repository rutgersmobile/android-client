package edu.rutgers.css.Rutgers.auxiliary;

import android.os.Bundle;

/**
 * Wraps an arg bundle which contains strings to be passed to ComponentFactory, including:
 * 	title		(required)
 * 	component	(required)
 * 	url			(optional)
 * 	data		(optional)
 */
public class SlideMenuItem {

	public Bundle args;
	
	/**
	 * Initialize menu item with a custom argument bundle
	 * @param args Custom argument bundle, which needs at least the title and component fields.
	 */
	public SlideMenuItem(Bundle args) {
		this.setArgs(args);
	}
	
	/**
	 * Initialize menu item with a simple argument bundle
	 * @param title Title argument
	 * @param component Component argument
	 */
	public SlideMenuItem(String title, String component) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("component", component);
		this.setArgs(args);
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
	
}
