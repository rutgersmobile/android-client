package edu.rutgers.css.Rutgers.channels.bus.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.api.bus.NextbusAPI;
import edu.rutgers.css.Rutgers.api.bus.model.Prediction;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import lombok.Data;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for Predictions
 */
public class PredictionLoader extends SimpleAsyncLoader<PredictionLoader.PredictionHolder> {

    public static final String TAG = "PredictionLoader";

    private String agency;
    private String tag;
    private String mode;

    private List<Prediction> oldData;

    @Data
    public class PredictionHolder {
        public final List<Prediction> predictions;
        public final String title;
    }

    /**
     * @param context Context of the application
     * @param agency Either New Brunswick or Newark
     * @param tag Route to get predictions for
     * @param mode Tells us to predict for Route or Stop
     */
    public PredictionLoader(Context context, String agency, String tag, String mode) {
        super(context);
        this.agency = agency;
        this.tag = tag;
        this.mode = mode;
        this.oldData = new ArrayList<>();
    }

    @Override
    public PredictionHolder loadInBackground() {
        List<Prediction> predictions = new ArrayList<>();
        String title = null;
        try {
            if (BusDisplay.ROUTE_MODE.equals(mode)) {
                predictions.addAll(NextbusAPI.routePredict(agency, tag));
                for (RouteStub route : NextbusAPI.getActiveRoutes(agency)) {
                    if (route.getTag().equals(tag)) {
                        title = route.getTitle();
                    }
                }
            } else if (BusDisplay.STOP_MODE.equals(mode)) {
                predictions.addAll(NextbusAPI.stopPredict(agency, tag));
                for (StopStub stop : NextbusAPI.getActiveStops(agency)) {
                    if (stop.getTag().equals(tag)) {
                        title = stop.getTitle();
                    }
                }
            }

            if (oldData.size() != predictions.size()) {
            /* Add items if the list is being newly populated, or
             * the updated JSON doesn't seem to match and the list should be
             * cleared and repopulated. */
                oldData.clear();
                oldData.addAll(predictions);
            } else {
            /* Update items individually if the list is already populated
             * and the new results correspond to currently displayed stops. */
                for (int i = 0; i < oldData.size(); i++) {
                    Prediction newPrediction = predictions.get(i);
                    Prediction oldPrediction = oldData.get(i);

                    if (!newPrediction.equals(oldPrediction)) {
                        LOGD(TAG, "Mismatched prediction: " + oldPrediction.getTitle() + " & " + newPrediction.getTitle());
                        oldPrediction.setTitle(newPrediction.getTitle());
                        oldPrediction.setTag(newPrediction.getTag());
                    }

                    oldPrediction.setMinutes(newPrediction.getMinutes());
                }
            }
        } catch (JsonSyntaxException | XmlPullParserException | IOException | IllegalArgumentException e) {
            LOGE(TAG, e.getMessage());
        }

        return new PredictionHolder(predictions, title);
    }
}
