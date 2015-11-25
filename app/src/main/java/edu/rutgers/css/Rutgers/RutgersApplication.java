package edu.rutgers.css.Rutgers;

import android.app.Application;
import android.content.Context;

import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.utils.MainActivityLifecycleHandler;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static Context context;


    private static MainActivityLifecycleHandler lh = new MainActivityLifecycleHandler();

    @Override
    public void onCreate() {
        super.onCreate();
        RutgersApplication.context = getApplicationContext();
        registerActivityLifecycleCallbacks(lh);

        LOGV(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);
    }

    public static Context getAppContext() {
        return RutgersApplication.context;
    }

    public static boolean isApplicationVisible() {
        return lh.isApplicationVisible();
    }

    public static boolean isApplicationInForeground() {
        return lh.isApplicationInForeground();
    }
}
