package edu.rutgers.css.Rutgers.channels.places.model.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.PlacesAPI;
import edu.rutgers.css.Rutgers.model.KeyValPair;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for places
 */
public class KeyValPairLoader extends AsyncTaskLoader<List<KeyValPair>> {

    public static final String TAG = "KeyValPairLoader";

    final String noneNearbyString;
    final String failedLoadString;
    private double lat;
    private double lon;

    private List<KeyValPair> data;

    public KeyValPairLoader(Context context, double lat, double lon) {
        super(context);
        noneNearbyString = context.getString(R.string.places_none_nearby);
        failedLoadString = context.getString(R.string.failed_load_short);
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public List<KeyValPair> loadInBackground() {
        List<KeyValPair> nearbyPlaces = new ArrayList<>();
        try {
            List<Place> places = PlacesAPI.getPlacesNear(lat, lon);
            if (places.isEmpty()) {
                nearbyPlaces.add(new KeyValPair(null, noneNearbyString));
            } else {
                for (Place place : places) {
                    nearbyPlaces.add(new KeyValPair(place.getId(), place.getTitle()));
                }
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            nearbyPlaces.add(new KeyValPair(null, failedLoadString));
        }
        return nearbyPlaces;
    }

    @Override
    public void deliverResult(List<KeyValPair> keyValPairs) {
        if (isReset()) {
            return;
        }

        List<KeyValPair> oldItems = data;
        data = keyValPairs;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        data = null;
    }
}
