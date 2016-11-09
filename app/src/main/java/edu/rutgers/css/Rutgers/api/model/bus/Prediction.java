package edu.rutgers.css.Rutgers.api.model.bus;

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
    private final List<Integer> minutes;
    private final List<List<Integer>> childItemSingleList;
    
    public Prediction (String title, String tag) {
        this.tag = tag;
        this.title = title;
        minutes = new ArrayList<>();
        childItemSingleList = new ArrayList<>();
        childItemSingleList.add(minutes);
    }

    public void addMinutes(int mins) { minutes.add(mins); }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Readable representation of prediction direction
     */
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * List of predicted arrival times in minutes
     */
    public List<Integer> getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return this.title + ", " + this.direction + ", " + this.minutes.toString();
    }

    @Override
    public List<?> getChildItemList() {
        return childItemSingleList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
