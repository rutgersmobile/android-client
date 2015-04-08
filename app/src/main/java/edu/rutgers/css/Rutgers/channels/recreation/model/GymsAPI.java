package edu.rutgers.css.Rutgers.channels.recreation.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

public final class GymsAPI {
    
    private static final String TAG = "Gyms";
    
    public static final DateFormat GYM_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy", Locale.US);
    
    private static final long expire = Request.CACHE_ONE_DAY; // Cache gym info for a day

    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();

    private GymsAPI() {}

    /**
     * Get all campuses from the Gyms API.
     * @return Promise for list of Campuses
     */
    public static Promise<List<Campus>, Exception, Void> getCampuses() {
        final DeferredObject<List<Campus>, Exception, Void> deferred = new DeferredObject<>();

        Promise<JSONArray, AjaxStatus, Double> p = Request.apiArray("gyms_array.txt", expire);

        sDM.when(p, AndroidExecutionScope.BACKGROUND).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                ArrayList<Campus> campuses = new ArrayList<>(result.length());
                Gson gson = new Gson();

                try {
                    for (int i = 0; i < result.length(); i++) {
                        Campus campus = gson.fromJson(result.getJSONObject(i).toString(), Campus.class);
                        campuses.add(campus);
                    }

                    deferred.resolve(campuses);
                } catch (JSONException | JsonSyntaxException e) {
                    LOGE(TAG, e.getMessage());
                    deferred.reject(e);
                }

            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                LOGE(TAG, AppUtils.formatAjaxStatus(result));
                deferred.reject(new Exception(AppUtils.formatAjaxStatus(result)));
            }
        });

        return deferred.promise();
    }

    /**
     * Get information for facility from campus.
     * @param campusTitle Campus title
     * @param facilityTitle Facility title
     * @return Promise for a facility. Fails if not found.
     */
    public static Promise<Facility, Exception, Void> getFacility(@NonNull final String campusTitle, @NonNull final String facilityTitle) {
        final Deferred<Facility, Exception, Void> deferred = new DeferredObject<>();

        sDM.when(getCampuses(), AndroidExecutionScope.BACKGROUND).done(new DoneCallback<List<Campus>>() {
            @Override
            public void onDone(List<Campus> result) {
                for (Campus campus : result) {
                    // Find the correct campus to search
                    if (!campusTitle.equalsIgnoreCase(campus.getTitle())) continue;

                    // Check for facility in this campus
                    Facility find = campus.getFacility(facilityTitle);
                    if (find != null) {
                        deferred.resolve(find);
                        return;
                    }
                }

                deferred.reject(new Exception("Facility not found"));
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                LOGE(TAG, result.getMessage());
                deferred.reject(result);
            }
        });

        return deferred.promise();
    }

}
