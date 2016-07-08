package edu.rutgers.css.Rutgers;

import android.app.Application;
import android.content.Context;

import edu.rutgers.css.Rutgers.api.Analytics;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        RutgersApplication.context = getApplicationContext();

        LOGV(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);
    }

    public static Context getAppContext() {
        return RutgersApplication.context;
    }
}
