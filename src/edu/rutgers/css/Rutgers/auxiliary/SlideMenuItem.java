package edu.rutgers.css.Rutgers.auxiliary;

import android.os.Bundle;

/**
 * Helper class for passing arguments to Component Factory. Fields include:
 * 	title		(required)
 * 	component	(required)
 * 	url			(optional)
 * 	data		(optional JSON object or array in string representation)
 */
public class SlideMenuItem {

	public Bundle args;
	
	/**
	 * Initialize menu item with a custom argument bundle.
	 * @param args Custom argument bundle, which needs at least the title and component fields.
	 */
	public SlideMenuItem(Bundle args) {
		this.setArgs(args);
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
	}
	
	/**
	 * Initialize menu item with channel title, component ID, and a URL argument.
	 * @param title
	 * @param component
	 * @param url
	 */
	public SlideMenuItem(String title, String component, String url) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("component", component);
		args.putString("url", url);
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
