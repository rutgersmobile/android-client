package edu.rutgers.css.Rutgers.api.athletics;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;

/**
 * Get athletics api data
 */
public class AthleticsAPI {
    public static AthleticsGames getGames(@NonNull final String resource) throws JsonSyntaxException, IOException {
        final String apiResource = "sports/" + resource + ".json";
        return ApiRequest.api(apiResource, AthleticsGames.class);
    }
}
