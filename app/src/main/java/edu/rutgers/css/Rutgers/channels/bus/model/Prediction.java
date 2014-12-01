package edu.rutgers.css.Rutgers.channels.bus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Arrival time predictions for a bus stop.
 */
public final class Prediction implements Serializable {
    private String tag;
    private String title;
    private String direction;
    private List<Integer> minutes;
    
    public Prediction (String title, String tag) {
        this.tag = tag;
        this.title = title;
        minutes = new ArrayList<>();
    }
    
    public Prediction (String title, String tag, String direction) {
        this.tag = tag;
        this.title = title;
        this.direction = direction;
        minutes = new ArrayList<>();
    }
    
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

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
    
    public List<Integer> getMinutes() {
        return minutes;
    }
    
    public void setMinutes(List<Integer> minutes) {
        this.minutes = minutes;
    }

    public void addMinutes(int mins) { minutes.add(mins); }

    @Override
    public String toString() {
        return this.title + ", " + this.direction + ", " + this.minutes.toString();
    }

    /**
     * Compares two predictions by the route or stop they represent, but not the minute values.
     * @param other Prediction to be compared to
     * @return True if tag, title, and direction match. False if not.
     */
    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(!(other instanceof Prediction)) return false;

        if(this.getTag() == null || this.getTitle() == null) return false;

        Prediction otherPrediction = (Prediction) other;
        return  this.getTag().equals(otherPrediction.getTag()) &&
                this.getTitle().equals(otherPrediction.getTitle()) &&
                (
                        (otherPrediction.getDirection() == null && this.getDirection() == null) ||
                        (this.getDirection() != null && this.getDirection().equals(otherPrediction.getDirection()))
                );
    }
    
}
