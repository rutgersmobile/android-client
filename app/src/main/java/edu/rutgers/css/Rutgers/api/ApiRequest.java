package edu.rutgers.css.Rutgers.api;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Type;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.interfaces.XmlParser;

/** Convenience class for making requests */
public final class ApiRequest {

    private static final String TAG = "ApiRequest";

    private static OkHttpClient client;

    public static long CACHE_NEVER = -1; // -1 means always refresh -- never use cache
    public static long CACHE_ONE_MINUTE = 1000 * 60;
    public static long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;

    private ApiRequest() {}

    /** Initialize the singleton OkHttp object */
    private static void setup () {
        if (client == null) {
            client = new OkHttpClient();
        }
    }

    /**
     * Parse JSON from the mobile server into an object of the given type
     * Works for JSON objects and arrays
     * @param resource JSON file to read from API directory
     * @param expire Cache time in milliseconds
     * @param type Type that the JSON should be parsed into
     * @param deserializer If not null register this deserializer with Gson before parsing. This can
     *                     be used for custom parsing of a JSON object when Gson alone won't do the
     *                     trick.
     * @param <T> Return type should be the same as type
     * @return An object of the given type
     */
    public static <T> T api(String resource, long expire, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        return json(Config.API_BASE + resource, expire, type, deserializer);
    }

    public static <T> T api(String resource, long expire, Type type) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, expire, type, null);
    }

    public static <T> T api(String resource, long expire, Class<T> clazz) throws JsonSyntaxException, IOException{
        return json(Config.API_BASE + resource, expire, (Type) clazz, null);
    }

    public static <T> T json(String resource, long expire, Type type) throws JsonSyntaxException, IOException {
        return json(resource, expire, type, null);
    }

    /**
     * Get arbitrary JSON.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @param type Type that the JSON should be parsed into
     * @param deserializer Optional custom deserializer.
     * @return Promise for a JSON object
     */
    public static <T> T json(String resource, long expire, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        setup();
        Response response = getResponse(resource);
        GsonBuilder gson = new GsonBuilder();
        if (deserializer != null) {
            gson = gson.registerTypeAdapter(type, deserializer);
        }

        return gson.create().fromJson(response.body().string(), type);
    }

    /**
     * Simple shortcut for getting a response from OkHttp
     * @param resource URL of the resource
     * @return A response object from executing a new call on the client
     * @throws IOException
     */
    public static Response getResponse(String resource) throws IOException {
        Request request = new Request.Builder()
                .url(resource)
                .build();
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
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static <T> T xml(String resource, long expire, XmlParser<T> parser) throws XmlPullParserException, IOException {
        setup();
        Response response = getResponse(resource);
        return parser.parse(response.body().byteStream());
    }
}
