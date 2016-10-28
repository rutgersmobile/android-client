package edu.rutgers.css.Rutgers.api;

import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.model.SimpleBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Retrofit API for Nextbus
 */

public interface NextBusService {
    @GET("service/publicXMLFeed?command=predictionsForMultiStops&a=rutgers")
    Observable<SimpleBody> predict(@Query("stops") List<QueryStop> stops);
}
