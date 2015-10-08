package edu.rutgers.css.Rutgers;

import android.app.Application;
import android.content.Context;

import com.androidquery.callback.BitmapAjaxCallback;

import java.util.Map;

import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.utils.MainActivityLifecycleHandler;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static Context context;

    private static Map<String, Channel> channelsMap;

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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Remove images from memory cache
        BitmapAjaxCallback.clearCache();
    }

    public static Context getAppContext() {
        return RutgersApplication.context;
    }

    public static void setChannelsMap(Map<String, Channel> channelsMap) {
        RutgersApplication.channelsMap = channelsMap;
    }

    public static Map<String, Channel> getChannelsMap() {
        return channelsMap;
    }

    public static boolean isApplicationVisible() {
        return lh.isApplicationVisible();
    }

    public static boolean isApplicationInForeground() {
        return lh.isApplicationInForeground();
    }
}
