package edu.rutgers.css.Rutgers.api.athletics;

import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Retrofit Athletics interface
 */

public interface AthleticsService {
    @GET("sports/{sport}.json")
    Observable<AthleticsGames> getGames(@Path("sport") String sport);
}
