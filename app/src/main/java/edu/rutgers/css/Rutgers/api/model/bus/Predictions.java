package edu.rutgers.css.Rutgers.api.model.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Used to collect a list of Predictions and their message (if it exists)
 */
public final class Predictions {
    private final Set<String> messages;
    private final List<Prediction> predictions;

    public Predictions(final Set<String> messages, final List<Prediction> predictions) {
        this.messages = messages;
        this.predictions = predictions;
    }

    /**
     * All messages that appear on this stop/route
     */
    public Set<String> getMessages() {
        return messages;
    }

    /**
     * List of all predictions made from query
     */
    public List<Prediction> getPredictions() {
        return predictions;
    }

    public List<Prediction> getPredictions(String vehicle) {
        final List<Prediction> filteredPredictions = new ArrayList<>();
        for (final Prediction prediction : predictions) {
            final List<VehiclePrediction> vehiclePredictions = prediction.getVehiclePredictions(vehicle);
            if (!vehiclePredictions.isEmpty()) {
                filteredPredictions.add(new Prediction(prediction.getTitle(), prediction.getTag(), vehiclePredictions));
            }
        }
        return filteredPredictions;
    }

    public void add(Predictions other) {
        messages.addAll(other.getMessages());
        predictions.addAll(other.getPredictions());
    }
}
