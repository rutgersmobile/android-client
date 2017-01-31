package edu.rutgers.css.Rutgers.api.model.bus;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

    public void setVehiclePredictions(Collection<? extends VehiclePrediction> vehiclePredictions) {
        this.vehiclePredictions.clear();
        this.vehiclePredictions.addAll(vehiclePredictions);
        childItemSingleList.clear();
        childItemSingleList.add(this.vehiclePredictions);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prediction that = (Prediction) o;

        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (direction != null ? !direction.equals(that.direction) : that.direction != null)
            return false;
        if (vehiclePredictions != null ? !vehiclePredictions.equals(that.vehiclePredictions) : that.vehiclePredictions != null)
            return false;
        return childItemSingleList != null ? childItemSingleList.equals(that.childItemSingleList) : that.childItemSingleList == null;

    }

    @Override
    public int hashCode() {
        int result = tag != null ? tag.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (vehiclePredictions != null ? vehiclePredictions.hashCode() : 0);
        result = 31 * result + (childItemSingleList != null ? childItemSingleList.hashCode() : 0);
        return result;
    }
}
