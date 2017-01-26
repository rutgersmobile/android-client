package edu.rutgers.css.Rutgers.api.model.bus;

import java.io.Serializable;

/**
 * Created by mattro on 1/26/17.
 */
public final class VehiclePrediction implements Serializable {
    private final String vehicle;
    private final Integer minutes;

    public VehiclePrediction(String vehicle, Integer minutes) {
        this.vehicle = vehicle;
        this.minutes = minutes;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public String getVehicle() {
        return vehicle;
    }

    @Override
    public String toString() {
        return minutes + " minutes";
    }
}
