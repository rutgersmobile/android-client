package edu.rutgers.css.Rutgers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import edu.rutgers.css.Rutgers.api.Analytics;

public class MyApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();

        Log.v(TAG, "Created");
        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}