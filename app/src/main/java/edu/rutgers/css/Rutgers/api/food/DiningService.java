package edu.rutgers.css.Rutgers.api.food;

import java.util.List;

import edu.rutgers.css.Rutgers.api.food.model.DiningMenu;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by mattro on 9/26/16.
 */

public interface DiningService {
    @GET("rutgers-dining.txt")
    Observable<List<DiningMenu>> getDiningHalls();
}
