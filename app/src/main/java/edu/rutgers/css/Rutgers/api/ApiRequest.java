package edu.rutgers.css.Rutgers.api;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.RutgersApplication;

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
        return new_json(Config.API_BASE + resource, expire, type);
    }

    /**
     * Get arbitrary JSON.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for a JSON object
     */
    public static <T> T new_json(String resource, long expire, Type type) throws JsonSyntaxException, IOException {
        setup();

        Request request = new Request.Builder()
                .url(resource)
                .build();
        Response response = client.newCall(request).execute();

        return new Gson().fromJson(response.body().string(), type);
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
