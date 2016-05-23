package edu.rutgers.css.Rutgers.channels.athletics.model.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.athletics.AthleticsAPI;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Loader for Athletics games
 */
public final class AthleticsGamesLoader extends SimpleAsyncLoader<AthleticsGames> {
    private final String TAG = "AthleticsGamesLoader";

    private final String resource;

    public AthleticsGamesLoader(Context context, final @NonNull String resource) {
        super(context);
        this.resource = resource;
    }

    @Override
    public AthleticsGames loadInBackground() {
        try {
            return AthleticsAPI.getGames(resource);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }
}
