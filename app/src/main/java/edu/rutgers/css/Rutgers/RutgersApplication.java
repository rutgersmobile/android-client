package edu.rutgers.css.Rutgers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.androidquery.callback.BitmapAjaxCallback;

import edu.rutgers.css.Rutgers.api.Analytics;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        RutgersApplication.context = getApplicationContext();

        Log.v(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);
    }

    @Override
    public void onLowMemory() {
        // Remove images from memory cache
        BitmapAjaxCallback.clearCache();
    }

    public static Context getAppContext() {
        return RutgersApplication.context;
    }

}