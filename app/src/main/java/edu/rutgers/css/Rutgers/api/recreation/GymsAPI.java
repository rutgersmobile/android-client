package edu.rutgers.css.Rutgers.api.recreation;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.recreation.model.Campus;
import edu.rutgers.css.Rutgers.api.recreation.model.facility.Facility;

public final class GymsAPI {
    
    private static final String TAG = "Gyms";
    
    public static final DateFormat GYM_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy", Locale.US);
    
    private static final int EXPIRE = 1; // Cache gym info for a day
    private static final TimeUnit EXPIRE_UNIT = TimeUnit.DAYS;

    private GymsAPI() {}

    /**
     * Get all campuses from the Gyms API.
     * @return Promise for list of Campuses
     */
    public static synchronized List<Campus> getCampuses() throws JsonSyntaxException, IOException {
        Type type = new TypeToken<List<Campus>>(){}.getType();
        return ApiRequest.api("gyms_array.txt", EXPIRE, EXPIRE_UNIT, type);
    }

    /**
     * Get information for facility from campus.
     * @param campusTitle Campus title
     * @param facilityTitle Facility title
     * @return Promise for a facility. Fails if not found.
     */
    public static synchronized Facility getFacility(@NonNull final String campusTitle, @NonNull final String facilityTitle) throws JsonSyntaxException, IOException {
        List<Campus> campuses = getCampuses();
        for (Campus campus : campuses) {
            // Find the correct campus to search
            if (!campusTitle.equalsIgnoreCase(campus.getTitle())) continue;

            // Check for facility in this campus
            Facility find = campus.getFacility(facilityTitle);
            if (find != null) {
                return find;
            }
        }

        return null;
    }

}
