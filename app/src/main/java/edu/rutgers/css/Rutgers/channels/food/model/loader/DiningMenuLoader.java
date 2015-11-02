package edu.rutgers.css.Rutgers.channels.food.model.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.channels.food.model.DiningAPI;
import edu.rutgers.css.Rutgers.channels.food.model.DiningMenu;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Async loader for dining hall meals
 */
public class DiningMenuLoader extends AsyncTaskLoader<DiningMenu> {

    public static final String TAG = "DiningMenuLoader";

    DiningMenu data;
    String location;
    Context context;
    DiningMenu newData;

    public DiningMenuLoader(Context context, DiningMenu data, String location) {
        super(context);
        this.context = context;
        this.data = data;
        this.location = location;
    }

    @Override
    public DiningMenu loadInBackground() {
        newData = null;
        try {
            newData = DiningAPI.getDiningLocation(location);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            AppUtils.showFailedLoadToast(context);
        }
        return newData;
    }

    @Override
    public void deliverResult(DiningMenu menu) {
        if (isReset()) {
            return;
        }

        DiningMenu oldItems = data;
        data = menu;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        data = null;
    }
}
