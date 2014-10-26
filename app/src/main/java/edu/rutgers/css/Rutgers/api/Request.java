package edu.rutgers.css.Rutgers.api;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/** Convenience class for making requests */
public class Request {
    
    private static final String TAG = "Request";

    private static AQuery sAq;
    
    public static long CACHE_NEVER = -1; // -1 means always refresh -- never use cache
    public static long CACHE_ONE_MINUTE = 1000 * 60;
    public static long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;
    
    private static void setup () {
        if (sAq == null) {
            sAq = new AQuery(RutgersApplication.getAppContext());
        }
    }

    /**
     * Get JSON from mobile server.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for a JSONObject
     */
    public static Promise<JSONObject, AjaxStatus, Double> api (String resource, long expire) {
        return json(AppUtil.API_BASE + resource, expire);
    }

    /**
     * Get arbitrary JSON.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for JSONObject
     */
    public static Promise<JSONObject, AjaxStatus, Double> json (String resource, long expire) {
        setup();
        final DeferredObject<JSONObject, AjaxStatus, Double> deferred = new DeferredObject<JSONObject, AjaxStatus, Double>();
        
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
    
    /**
     * Gets arbitrary JSON array.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for JSONArray
     */
    public static Promise<JSONArray, AjaxStatus, Double> jsonArray (String resource, long expire) {
        setup();
        final DeferredObject<JSONArray, AjaxStatus, Double> deferred = new DeferredObject<JSONArray, AjaxStatus, Double>();
        
        sAq.ajax(resource, JSONArray.class, expire, new AjaxCallback<JSONArray>() {

            @Override
            public void callback(String url, JSONArray jsonArray, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if (jsonArray == null) {
                    status.invalidate();
                    deferred.reject(status);
                } else deferred.resolve(jsonArray);
            }

        });
        
        return deferred.promise();
    }
    
    /**
     * Get arbitrary XML.
     * @param resource XML file URL
     * @param expire Cache time in milliseconds
     * @return Promise for XmlDom
     */
    public static Promise<XmlDom, AjaxStatus, Double> xml (String resource, long expire) {
        setup();
        final DeferredObject<XmlDom, AjaxStatus, Double> deferred = new DeferredObject<XmlDom, AjaxStatus, Double>();
        
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
    
}
