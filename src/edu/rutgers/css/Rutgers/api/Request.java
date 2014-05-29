package edu.rutgers.css.Rutgers.api;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import edu.rutgers.css.Rutgers.MyApplication;

// Convenience class for making requests
public class Request {
	
	private static final String TAG = "Request";
	private static final String API_BASE = "http://sauron.rutgers.edu/~rfranknj/newmobile/";
	private static AQuery aq;
	private static boolean mSetupDone = false;
	
	public static long EXPIRE_ALWAYS = -1; // -1 means always refresh -- never use cache
	public static long EXPIRE_ONE_HOUR = 1000 * 60 * 60;
	
	private static void setup () {
		if (!mSetupDone) {
			aq = new AQuery(MyApplication.getAppContext());
			
			mSetupDone = true;
		}
	}
	
	// Makes a call against the api. Expects a JSON object
	public static Promise<JSONObject, AjaxStatus, Double> api (String resource) {
		return json(API_BASE + resource, EXPIRE_ALWAYS);
	}
	
	// gets arbitrary json
	public static Promise<JSONObject, AjaxStatus, Double> json (String resource, long expire) {
		setup();
		final DeferredObject<JSONObject, AjaxStatus, Double> deferred = new DeferredObject<JSONObject, AjaxStatus, Double>();
		
		aq.ajax(resource, JSONObject.class, expire, new AjaxCallback<JSONObject>() {

			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
				if(status.getCode() == AjaxStatus.TRANSFORM_ERROR) {
					status.invalidate();
					deferred.reject(status);
				}
				else if (json != null) deferred.resolve(json);
				else deferred.reject(status);
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
		
		aq.ajax(resource, JSONArray.class, expire, new AjaxCallback<JSONArray>() {

			@Override
			public void callback(String url, JSONArray jsonArray, AjaxStatus status) {
				if(status.getCode() == AjaxStatus.TRANSFORM_ERROR) {
					status.invalidate();
					deferred.reject(status);
				}
				else if(jsonArray != null) deferred.resolve(jsonArray);
				else deferred.reject(status);
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
				if(status.getCode() == AjaxStatus.TRANSFORM_ERROR) {
					status.invalidate();
					deferred.reject(status);
				}
				else if (xml != null) deferred.resolve(xml);
				else deferred.reject(status);
			}
			
		});
		
		
		return deferred.promise();
	}
}
