package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for dining hall meals
 */
public class DiningMenuLoader extends SimpleAsyncLoader<DiningMenu> {

    public static final String TAG = "DiningMenuLoader";

    String location;

    public DiningMenuLoader(Context context, String location) {
        super(context);
        this.location = location;
    }

    @Override
    public DiningMenu loadInBackground() {
        DiningMenu diningMenu = null;
        try {
            diningMenu = DiningAPI.getDiningLocation(location);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }
        return diningMenu;
    }
}
