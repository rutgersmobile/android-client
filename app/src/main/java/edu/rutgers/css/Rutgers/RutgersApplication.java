package edu.rutgers.css.Rutgers;

import android.support.multidex.MultiDexApplication;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.RutgersAPI;
import edu.rutgers.css.Rutgers.api.SOCAPI;
import edu.rutgers.css.Rutgers.oldapi.Analytics;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

public class RutgersApplication extends MultiDexApplication {

    private static final String TAG = "RutgersApplication";

    private static OkHttpClient client;

    public static OkHttpClient getClient() {
        return client;
    }

    public void onCreate() {
        super.onCreate();

        LOGV(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);

        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
            .cache(new Cache(getCacheDir(), 10 * 1024 * 1024))
//            .addInterceptor(interceptor)
            .build();

        RutgersAPI.simpleSetup(client, Config.API_BASE);
        NextbusAPI.simpleSetup(client, Config.NB_API_BASE);
        SOCAPI.simpleSetup(client, Config.SOC_API_BASE);

        Picasso picasso = new Picasso.Builder(this)
            .downloader(new OkHttp3Downloader(client))
            .loggingEnabled(true)
//             uncomment if you want to see if an image was cached
//            .indicatorsEnabled(true)
            .build();

        Picasso.setSingletonInstance(picasso);
    }
}
