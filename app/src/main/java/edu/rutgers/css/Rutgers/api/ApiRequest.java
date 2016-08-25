package edu.rutgers.css.Rutgers.api;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import lombok.Getter;

/** Convenience class for making requests */
public final class ApiRequest {

    private static final String TAG = "ApiRequest";

    @Getter
    private static final OkHttpClient client = new OkHttpClient();

    private static final int CACHE_NEVER = -1; // -1 means always refresh -- never use cache

    private ApiRequest() {}

    /**
     * This should be in setup, but since it requires a
     * context to get the directory, it needs to to be called
     * by itself in onCreate in MainActivity
     * @param context Context to get cache directory from
     */
    public static void enableCache(Context context, int cacheSize) {
        Cache cache = new Cache(context.getCacheDir(), cacheSize);
        client.setCache(cache);
    }

    /**
     * Parse JSON from the mobile server into an object of the given type
     * Works for JSON objects and arrays.
     * @param resource JSON file to read from API directory
     * @param expire Cache time in milliseconds. If omitted, defaults to 1, must be provided with unit
     * @param unit TimeUnit to say how long to cache. If omitted, then no caching
     * @param type Type that the JSON should be parsed into
     * @param deserializer If not null register this deserializer with Gson before parsing. This can
     *                     be used for custom parsing of a JSON object when Gson alone won't do the
     *                     trick.
     * @param <T> Return type should be the same as type
     * @return An object of the given type
     */
    public static <T> T api(String resource, int expire, TimeUnit unit, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        return json(Config.API_BASE + resource, expire, unit, type, deserializer);
    }

    public static <T> T api(String resource, TimeUnit unit, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        return json(Config.API_BASE + resource, 1, unit, type, deserializer);
    }

    public static <T> T api(String resource, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        return json(Config.API_BASE + resource, CACHE_NEVER, TimeUnit.DAYS, type, deserializer);
    }

    public static <T> T api(String resource, int expire, TimeUnit unit, Type type) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, expire, unit, type, null);
    }

    public static <T> T api(String resource, TimeUnit unit, Type type) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, 1, unit, type, null);
    }

    public static <T> T api(String resource, Type type) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, CACHE_NEVER, TimeUnit.DAYS, type, null);
    }

    public static <T> T api(String resource, int expire, TimeUnit unit, Class<T> clazz) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, expire, unit, (Type) clazz, null);
    }

    public static <T> T api(String resource, TimeUnit unit, Class<T> clazz) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, 1, unit, (Type) clazz, null);
    }

    public static <T> T api(String resource, Class<T> clazz) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, CACHE_NEVER, TimeUnit.DAYS, (Type) clazz, null);
    }

    /**
     * Get arbitrary JSON.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @param type Type that the JSON should be parsed into
     * @param deserializer Optional custom deserializer.
     * @return Promise for a JSON object
     */
    public static <T> T json(String resource, int expire, TimeUnit unit, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        Response response = getResponse(resource, expire, unit);
        GsonBuilder gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new GsonUTCDateAdapter());
        if (deserializer != null) {
            gson = gson.registerTypeAdapter(type, deserializer);
        }

        return gson.create().fromJson(response.body().charStream(), type);
    }

    public static <T> T json(String resource, int expire, TimeUnit unit, Type type) throws JsonSyntaxException, IOException {
        return json(resource, expire, unit, type, null);
    }

    public static <T> T json(String resource, TimeUnit unit, Type type) throws JsonSyntaxException, IOException {
        return json(resource, 1, unit, type, null);
    }

    public static <T> T json(String resource, Type type) throws JsonSyntaxException, IOException {
        return json(resource, CACHE_NEVER, TimeUnit.DAYS, type, null);
    }

    /**
     * Simple shortcut for getting a response from OkHttp
     * @param resource URL of the resource
     * @return A response object from executing a new call on the client
     * @throws IOException
     */
    public static Response getResponse(String resource, int expire, TimeUnit unit) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(resource);

        CacheControl control;
        if (expire != CACHE_NEVER) {
            control = new CacheControl.Builder()
                    .maxStale(expire, unit)
                    .build();
        } else {
            control = new CacheControl.Builder()
                    .noStore()
                    .build();
        }

        Request request = builder.cacheControl(control).build();
        return client.newCall(request).execute();
    }

    /**
     * Get arbitrary xml
     * @param resource URL of xml file
     * @param expire Cache time in milliseconds
     * @param parser XmlParser used to parse xml into an object. Typically implemented with an
     *               XmlPullParser
     * @param <T> Type of object to be returned from the parser
     * @return Object parsed from xml
     * @throws ParseException
     * @throws IOException
     */
    public static <T> T xml(String resource, int expire, TimeUnit unit, XmlParser<T> parser) throws ParseException, IOException {
        Response response = getResponse(resource, expire, unit);
        parser.setResponse(response);
        return parser.parse(response.body().byteStream());
    }

    public static <T> T xml(String resource, TimeUnit unit, XmlParser<T> parser) throws ParseException, IOException {
        return xml(resource, 1, unit, parser);
    }

    public static <T> T xml(String resource, XmlParser<T> parser) throws ParseException, IOException {
        return xml(resource, CACHE_NEVER, TimeUnit.DAYS, parser);
    }
}
