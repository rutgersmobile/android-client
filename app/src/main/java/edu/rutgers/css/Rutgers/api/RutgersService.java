package edu.rutgers.css.Rutgers.api;

import java.util.List;

import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;
import edu.rutgers.css.Rutgers.api.bus.model.ActiveStops;
import edu.rutgers.css.Rutgers.api.bus.model.AgencyConfig;
import edu.rutgers.css.Rutgers.api.cinema.model.Movie;
import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.api.places.PlacesAPI;
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
    Observable<PlacesAPI.KVHolder> getPlacesMap();
}
