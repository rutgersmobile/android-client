package edu.rutgers.css.Rutgers.api.service;

import com.google.gson.JsonArray;

import java.util.List;

import edu.rutgers.css.Rutgers.api.model.athletics.AthleticsGames;
import edu.rutgers.css.Rutgers.api.model.bus.ActiveStops;
import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
import edu.rutgers.css.Rutgers.api.model.cinema.Movie;
import edu.rutgers.css.Rutgers.api.model.food.DiningMenu;
import edu.rutgers.css.Rutgers.api.model.places.KVHolder;
import edu.rutgers.css.Rutgers.api.model.soc.SOCIndex;
import edu.rutgers.css.Rutgers.api.model.soc.Semesters;
import edu.rutgers.css.Rutgers.api.model.Motd;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Retrofit Athletics interface
 */

public interface RutgersService {
    @GET("sports/{sport}.json")
    Observable<AthleticsGames> getGames(@Path("sport") String sport);

    @GET("rutgers-dining.txt")
    Observable<List<DiningMenu>> getDiningHalls();

    @GET("cinema.json")
    Observable<List<Movie>> getMovies();

    @GET("nbactivestops.txt")
    Observable<ActiveStops> getNewBrunswickActiveStops();

    @GET("nwkactivestops.txt")
    Observable<ActiveStops> getNewarkActiveStops();

    @GET("rutgersrouteconfig.txt")
    Observable<AgencyConfig> getNewBrunswickAgencyConfig();

    @GET("rutgers-newarkrouteconfig.txt")
    Observable<AgencyConfig> getNewarkAgencyConfig();

    @GET("places.txt")
    Observable<KVHolder> getPlacesMap();

    @GET("soc_conf.txt")
    Observable<Semesters> getSemesters();

    @GET("indexes/{semesterCode}_{campusCode}_{levelCode}.json")
    Observable<SOCIndex> getSOCIndex(
        @Path("semesterCode") String semesterCode,
        @Path("campusCode") String campusCode,
        @Path("levelCode") String levelCode
    );

    @GET("motd.txt")
    Observable<Motd> getMotd();

    @GET("ordered_content.json")
    Observable<JsonArray> getOrderedContent();
}
