package edu.rutgers.css.Rutgers;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ApiRequest;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        LOGV(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);

        ApiRequest.enableCache(this, 10 * 1024 * 1024);
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(ApiRequest.getClient()))
                .build();
        // uncomment if you want to see if an image was cached
//        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);
        Picasso.setSingletonInstance(picasso);
    }
}
