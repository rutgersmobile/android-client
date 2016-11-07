package edu.rutgers.css.Rutgers;

import android.app.Application;

import com.google.gson.GsonBuilder;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.bus.model.AgencyConfig;
import edu.rutgers.css.Rutgers.api.bus.parsers.AgencyConfigDeserializer;
import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.api.soc.parsers.SOCIndexDeserializer;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

public class RutgersApplication extends Application {

    private static final String TAG = "RutgersApplication";

    private static OkHttpClient client;

    public static OkHttpClient getClient() {
        return client;
    }

    public static Retrofit retrofit;

    public static Retrofit nbRetrofit;

    public static Retrofit socRetrofit;

    public void onCreate() {
        super.onCreate();

        LOGV(TAG, "Application started");

        // Queue "app launched" event
        Analytics.queueEvent(this, Analytics.LAUNCH, null);

        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
            .cache(new Cache(getCacheDir(), 10 * 1024 * 1024))
            .addInterceptor(interceptor)
            .build();

        // AgencyConfig has a special deserializer because the JSON is a weird format
        retrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(
                new GsonBuilder()
                    .registerTypeAdapter(AgencyConfig.class, new AgencyConfigDeserializer())
                    .registerTypeAdapter(SOCIndex.class, new SOCIndexDeserializer())
                    .create()
            ))
            .client(client)
            .baseUrl(Config.API_BASE)
            .build();

        // retrofit instance just for Nextbus
        nbRetrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(client)
            .baseUrl(Config.NB_API_BASE)
            .build();

        socRetrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Config.SOC_API_BASE)
            .build();

        Picasso picasso = new Picasso.Builder(this)
            .downloader(new OkHttp3Downloader(client))
            .build();

        // uncomment if you want to see if an image was cached
//        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);
        Picasso.setSingletonInstance(picasso);
    }
}
