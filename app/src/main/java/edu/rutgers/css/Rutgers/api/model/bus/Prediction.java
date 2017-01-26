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
    private final List<VehiclePrediction> vehiclePredictions;
    private final List<List<VehiclePrediction>> childItemSingleList;

    public Prediction (String title, String tag) {
        this.tag = tag;
        this.title = title;
        vehiclePredictions = new ArrayList<>();
        childItemSingleList = new ArrayList<>();
        childItemSingleList.add(vehiclePredictions);
    }

    public Prediction(String title, String tag, List<VehiclePrediction> vehiclePredictions) {
        this.tag = tag;
        this.title = title;
        this.vehiclePredictions = vehiclePredictions;
        childItemSingleList = new ArrayList<>();
        childItemSingleList.add(vehiclePredictions);
    }

    public void addVehiclePrediction(VehiclePrediction prediction) {
        vehiclePredictions.add(prediction);
    }

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
     * List of predicted arrival times in vehiclePredictions
     */
    public List<VehiclePrediction> getVehiclePredictions() {
        return vehiclePredictions;
    }

    public List<VehiclePrediction> getVehiclePredictions(String vehicle) {
        final List<VehiclePrediction> filteredPredictions = new ArrayList<>();
        for (final VehiclePrediction vehiclePrediction : vehiclePredictions) {
            if (vehiclePrediction.getVehicle().equals(vehicle)) {
                filteredPredictions.add(vehiclePrediction);
            }
        }
        return filteredPredictions;
    }

    @Override
    public String toString() {
        return this.title + ", " + this.direction + ", " + this.vehiclePredictions.toString();
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
