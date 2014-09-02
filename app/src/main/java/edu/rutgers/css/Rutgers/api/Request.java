package edu.rutgers.css.Rutgers.api;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.MyApplication;
import edu.rutgers.css.Rutgers.utils.AppUtil;

// Convenience class for making requests
public class Request {
	
	private static final String TAG = "Request";
	//private static final String API_BASE = "http://sauron.rutgers.edu/~rfranknj/newmobile/";
	private static AQuery aq;
	private static boolean mSetupDone = false;
	
	public static long CACHE_NEVER = -1; // -1 means always refresh -- never use cache
    public static long CACHE_ONE_MINUTE = 1000 * 60;
	public static long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;
	
	private static void setup () {
		if (!mSetupDone) {
			aq = new AQuery(MyApplication.getAppContext());
			
			mSetupDone = true;
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
     * Get full AJAX callback object for JSON from mobile server.
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return Promise for an AJAX callback object
     */
    public static Promise<AjaxCallback<JSONObject>, AjaxStatus, Double> apiWithStatus(String resource, long expire) {
        return jsonWithStatus(AppUtil.API_BASE + resource, expire);
    }

    /**
     * Get JSON from mobile server, synchronously (blocking).
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return AjaxCallback for JSONObject
     */
    public static AjaxCallback<JSONObject> apiSynchronous(String resource, long expire) {
        return jsonSynchronous(AppUtil.API_BASE + resource, expire);
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
		
		aq.ajax(resource, JSONObject.class, expire, new AjaxCallback<JSONObject>() {

			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
				if(json == null) {
					status.invalidate();
					deferred.reject(status);
				}
				else deferred.resolve(json);
			}
			
		});
		
		return deferred.promise();
	}

    /**
     * Get arbitrary JSON, in full AJAX callback.
     * @param resource
     * @param expire
     * @return
     */
    public static Promise<AjaxCallback<JSONObject>, AjaxStatus, Double> jsonWithStatus(String resource, long expire) {
        setup();
        final DeferredObject<AjaxCallback<JSONObject>, AjaxStatus, Double> deferred = new DeferredObject<AjaxCallback<JSONObject>, AjaxStatus, Double>();

        aq.ajax(resource, JSONObject.class, expire, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if(json == null) {
                    status.invalidate();
                    deferred.reject(status);
                }
                else deferred.resolve(this);
            }
        });

        return deferred.promise();
    }

    /**
     * Get arbitrary JSON synchronously (blocking).
     * @param resource JSON file URL
     * @param expire Cache time in milliseconds
     * @return AjaxCallback for JSONObject
     */
    public static AjaxCallback<JSONObject> jsonSynchronous(String resource, long expire) {
        setup();
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>();
        callback.url(resource).expire(expire).type(JSONObject.class);
        aq.sync(callback);
        // Don't cache if we didn't get a valid object
        if(callback.getStatus().getCode() == AjaxStatus.TRANSFORM_ERROR) {
            callback.getStatus().invalidate();
        }
        return callback;
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
		
		aq.ajax(resource, JSONArray.class, expire, new AjaxCallback<JSONArray>() {

			@Override
			public void callback(String url, JSONArray jsonArray, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if(jsonArray == null) {
					status.invalidate();
					deferred.reject(status);
				}
				else deferred.resolve(jsonArray);
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
		
		aq.ajax(resource, XmlDom.class, expire, new AjaxCallback<XmlDom>() {

			@Override
			public void callback(String url, XmlDom xml, AjaxStatus status) {
                // Don't cache if we didn't get a valid object
                if(xml == null) {
					status.invalidate();
					deferred.reject(status);
				}
				else deferred.resolve(xml);
			}
			
		});
		
		return deferred.promise();
	}
	
}
