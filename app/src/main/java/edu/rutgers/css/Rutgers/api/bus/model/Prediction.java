package edu.rutgers.css.Rutgers.api.bus.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Arrival time predictions for a bus stop.
 */
public final class Prediction implements Serializable, ParentListItem {
    private String tag;
    private String title;
    private String direction;
    private List<Integer> minutes;
    
    public Prediction (String title, String tag) {
        this.tag = tag;
        this.title = title;
        minutes = new ArrayList<>();
    }

    public void addMinutes(int mins) { minutes.add(mins); }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<Integer> getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return this.title + ", " + this.direction + ", " + this.minutes.toString();
    }

    @Override
    public List<?> getChildItemList() {
        return null;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
