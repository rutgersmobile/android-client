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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VehiclePrediction that = (VehiclePrediction) o;

        if (vehicle != null ? !vehicle.equals(that.vehicle) : that.vehicle != null) return false;
        return minutes != null ? minutes.equals(that.minutes) : that.minutes == null;

    }

    @Override
    public int hashCode() {
        int result = vehicle != null ? vehicle.hashCode() : 0;
        result = 31 * result + (minutes != null ? minutes.hashCode() : 0);
        return result;
    }
}
