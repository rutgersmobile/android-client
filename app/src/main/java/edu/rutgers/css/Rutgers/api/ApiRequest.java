package edu.rutgers.css.Rutgers.api;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Type;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.interfaces.XmlParser;

/** Convenience class for making requests */
public final class ApiRequest {

    private static final String TAG = "ApiRequest";

    private static AQuery sAq;

    private static OkHttpClient client;

    public static long CACHE_NEVER = -1; // -1 means always refresh -- never use cache
    public static long CACHE_ONE_MINUTE = 1000 * 60;
    public static long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;

    private ApiRequest() {}

    /** Initialize the singleton AQuery object */
    private static void setup () {
        if (sAq == null) {
            sAq = new AQuery(RutgersApplication.getAppContext());
        }
        if (client == null) {
            client = new OkHttpClient();
        }
    }

    /**
     * Get JSON object from mobile server.
     * @param resource JSON file to read from API directory
     * @param expire Cache time in milliseconds
     * @return Promise for a JSON object
     */
    public static <T> T new_api(String resource, long expire, Type type) throws JsonSyntaxException, IOException{
        return new_json(Config.API_BASE + resource, expire, type, null);
    }

    public static <T> T new_api(String resource, long expire, Class<T> clazz) throws JsonSyntaxException, IOException{
        return new_json(Config.API_BASE + resource, expire, (Type) clazz, null);
    }

    public static <T> T new_api(String resource, long expire, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        return new_json(Config.API_BASE + resource, expire, type, deserializer);
    }

    /**
     * Get arbitrary JSON.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for a JSON object
     */
    public static <T> T new_json(String resource, long expire, Type type, JsonDeserializer<T> deserializer) throws JsonSyntaxException, IOException {
        setup();
        Response response = getResponse(resource);
        GsonBuilder gson = new GsonBuilder();
        if (deserializer != null) {
            gson = gson.registerTypeAdapter(type, deserializer);
        }

        return gson.create().fromJson(response.body().string(), type);
    }

    private static Response getResponse(String resource) throws IOException {
        Request request = new Request.Builder()
                .url(resource)
                .build();
        return client.newCall(request).execute();
    }

    public static <T> T new_xml(String resource, long expire, XmlParser<T> parser) throws XmlPullParserException, IOException {
        setup();
        Response response = getResponse(resource);
        return parser.parse(response.body().byteStream());
    }

    /**
     * Get arbitrary XML.
     * @param resource XML file URL
     * @param expire Cache time in milliseconds
     * @return Promise for XmlDom
     */
    public static Promise<XmlDom, AjaxStatus, Double> xml (String resource, long expire) {
        setup();
        final DeferredObject<XmlDom, AjaxStatus, Double> deferred = new DeferredObject<>();

        sAq.ajax(resource, XmlDom.class, expire, new AjaxCallback<XmlDom>() {

            @Override
            public void callback(String url, XmlDom xml, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if (xml == null) {
                    status.invalidate();
                    deferred.reject(status);
                } else deferred.resolve(xml);
            }

        });

        return deferred.promise();
    }

    public static Promise<JSONObject, AjaxStatus, Double> api (String resource, long expire) {
            return json(Config.API_BASE + resource, expire);
    }

    public static Promise<JSONArray, AjaxStatus, Double> apiArray(String resource, long expire) {
        return jsonArray(Config.API_BASE + resource, expire);
    }

    public static Promise<JSONObject, AjaxStatus, Double> json (String resource, long expire) {
        setup();
        final DeferredObject<JSONObject, AjaxStatus, Double> deferred = new DeferredObject<>();

        sAq.ajax(resource, JSONObject.class, expire, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if (json == null) {
                    status.invalidate();
                    deferred.reject(status);
                } else deferred.resolve(json);
            }

        });

        return deferred.promise();
    }

    public static Promise<JSONArray, AjaxStatus, Double> jsonArray (String resource, long expire) {
        setup();
        final DeferredObject<JSONArray, AjaxStatus, Double> deferred = new DeferredObject<>();

        sAq.ajax(resource, JSONArray.class, expire, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray jsonArray, AjaxStatus status) {
                if (jsonArray == null) {
                    status.invalidate();
                    deferred.reject(status);
                } else deferred.resolve(jsonArray);
            }
        });

        return deferred.promise();
    }
}
