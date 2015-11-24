package edu.rutgers.css.Rutgers.channels.places.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.places.model.PlacesAPI;
import edu.rutgers.css.Rutgers.model.KeyValPair;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import lombok.val;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for places
 */
public class KeyValPairLoader extends SimpleAsyncLoader<List<KeyValPair>> {

    public static final String TAG = "KeyValPairLoader";

    final String noneNearbyString;
    final String failedLoadString;
    private double lat;
    private double lon;

    public KeyValPairLoader(Context context, double lat, double lon) {
        super(context);
        noneNearbyString = context.getString(R.string.places_none_nearby);
        failedLoadString = context.getString(R.string.failed_load_short);
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public List<KeyValPair> loadInBackground() {
        val nearbyPlaces = new ArrayList<KeyValPair>();
        try {
            val places = PlacesAPI.getPlacesNear(lat, lon);
            if (places.isEmpty()) {
                nearbyPlaces.add(new KeyValPair(null, noneNearbyString));
            } else {
                for (val place : places) {
                    nearbyPlaces.add(new KeyValPair(place.getId(), place.getTitle()));
                }
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            nearbyPlaces.add(new KeyValPair(null, failedLoadString));
        }
        return nearbyPlaces;
    }
}
