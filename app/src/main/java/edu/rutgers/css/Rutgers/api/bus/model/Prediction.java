package edu.rutgers.css.Rutgers.api.bus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Arrival time predictions for a bus stop.
 */
@Data
@EqualsAndHashCode(exclude = "minutes")
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
    
    public void addMinutes(int mins) { minutes.add(mins); }

    @Override
    public String toString() {
        return this.title + ", " + this.direction + ", " + this.minutes.toString();
    }
}
