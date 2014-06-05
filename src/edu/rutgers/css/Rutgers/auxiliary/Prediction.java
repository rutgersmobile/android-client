package edu.rutgers.css.Rutgers.auxiliary;

import java.util.ArrayList;

/**
 * Bus arrival time prediction
 *
 */
public class Prediction {
	private String tag;
	private String title;
	private String direction;
	private ArrayList<Integer> minutes;
	
	public Prediction (String title, String tag) {
		this.tag = tag;
		this.title = title;
		minutes = new ArrayList<Integer>();
	}
	
	public Prediction (String title, String tag, String direction) {
		this.tag = tag;
		this.title = title;
		this.direction = direction;
		minutes = new ArrayList<Integer>();
	}
	
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void addPrediction (int mins) { minutes.add(mins); }
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public ArrayList<Integer> getMinutes() {
		return minutes;
	}
	
	public void setMinutes(ArrayList<Integer> minutes) {
		this.minutes = minutes;
	}

	@Override
	public String toString() {
		return this.title + "," + this.direction + "," + this.minutes.toString();
	}
	
}
