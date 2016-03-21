package edu.rutgers.css.Rutgers.channels.recreation.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.recreation.model.facility.Facility;
import edu.rutgers.css.Rutgers.api.recreation.GymsAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader to get a facility associated with a campus and a name
 */
public class FacilityLoader extends SimpleAsyncLoader<Facility> {
    String campusName;
    String facilityName;

    public static final String TAG = "FacilityLoader";

    public FacilityLoader(Context context, String campusName, String facilityName) {
        super(context);
        this.campusName = campusName;
        this.facilityName = facilityName;
    }

    @Override
    public Facility loadInBackground() {
        try {
            return GymsAPI.getFacility(campusName, facilityName);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }
}
