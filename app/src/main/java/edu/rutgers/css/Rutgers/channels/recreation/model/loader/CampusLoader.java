package edu.rutgers.css.Rutgers.channels.recreation.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.recreation.model.Campus;
import edu.rutgers.css.Rutgers.channels.recreation.model.GymsAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for the gym api
 */
public class CampusLoader extends SimpleAsyncLoader<List<Campus>> {
    public static final String TAG = "CampusLoader";

    public CampusLoader(Context context) {
        super(context);
    }

    @Override
    public List<Campus> loadInBackground() {
        try {
            return GymsAPI.getCampuses();
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }
}
