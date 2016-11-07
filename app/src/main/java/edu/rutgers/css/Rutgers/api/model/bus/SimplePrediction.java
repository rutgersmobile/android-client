package edu.rutgers.css.Rutgers.api.model.bus;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Simple XML prediction item
 */

@Root(name = "prediction")
public class SimplePrediction {
    @Attribute
    private long epochTime;

    @Attribute
    private int seconds;

    @Attribute
    private int minutes;

    @Attribute
    private boolean isDeparture;

    @Attribute
    private String dirTag;

    @Attribute
    private String vehicle;

    @Attribute
    private String block;

    @Attribute(required = false)
    private boolean affectedByLayover;

    public SimplePrediction() {}

    public SimplePrediction(long epochTime, int seconds, int minutes,
                            boolean isDeparture, String dirTag, String vehicle,
                            String block, boolean affectedByLayover) {
        this.epochTime = epochTime;
        this.seconds = seconds;
        this.minutes = minutes;
        this.isDeparture = isDeparture;
        this.dirTag = dirTag;
        this.vehicle = vehicle;
        this.block = block;
        this.affectedByLayover = affectedByLayover;
    }

    public long getEpochTime() {
        return epochTime;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean isDeparture() {
        return isDeparture;
    }

    public String getDirTag() {
        return dirTag;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getBlock() {
        return block;
    }

    public boolean getAffectedByLayover() {
        return affectedByLayover;
    }
}
