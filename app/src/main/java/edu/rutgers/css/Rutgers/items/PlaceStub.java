package edu.rutgers.css.Rutgers.items;

/**
 * Created by James on 9/3/2014.
 */

public class PlaceStub implements Comparable<PlaceStub> {

    private String key;
    private String title;
    private float distance;

    public PlaceStub(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(PlaceStub other) {
        if(other == null) return -1;
        return other.getTitle().compareTo(this.getTitle());
    }

    @Override
    public String toString() {
        return title;
    }

}
