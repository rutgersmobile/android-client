package edu.rutgers.css.Rutgers.channels.food.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Helper for getting data from dining API
 *
 */
public class Dining {

    private static final String TAG = "DiningAPI";

    private static final String API_PATH = "rutgers-dining.txt";
    private static long expire = Request.CACHE_ONE_HOUR; // Cache dining data for an hour

    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();
    private static Promise<Object, Exception, Void> configured;
    private static List<DiningMenu> mNBDiningMenus;

    /**
     * Grab the dining API data.
     * <p>(Current API only has New Brunswick data; when multiple confs need to be read set this up like Nextbus.java)</p>
     */
    private static void setup() {
        // Get JSON array from dining API
        final Deferred<Object, Exception, Void> confd = new DeferredObject<Object, Exception, Void>();
        configured = confd.promise();
        
        final Promise<JSONArray, AjaxStatus, Double> promiseNBDining = Request.apiArray(API_PATH, expire);

        sDM.when(promiseNBDining, AndroidExecutionScope.BACKGROUND).done(new DoneCallback<JSONArray>() {

            @Override
            public void onDone(JSONArray res) {
                Gson gson = new Gson();

                mNBDiningMenus = new ArrayList<DiningMenu>(4);
                try {
                    for (int i = 0; i < res.length(); i++) {
                        DiningMenu diningMenu = gson.fromJson(res.getJSONObject(i).toString(), DiningMenu.class);
                        mNBDiningMenus.add(diningMenu);
                    }
                } catch (JSONException | JsonSyntaxException e) {
                    Log.e(TAG, "setup(): " + e.getMessage());
                    confd.reject(e);
                }

                confd.resolve(null);
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus e) {
                Log.e(TAG, AppUtil.formatAjaxStatus(e));
                confd.reject(new Exception(AppUtil.formatAjaxStatus(e)));
            }

        });
    }
    
    /**
     * Get all dining hall menus.
     * @return List of all dining hall menus
     */
    public static Promise<List<DiningMenu>, Exception, Void> getDiningHalls() {
        final Deferred<List<DiningMenu>, Exception, Void> d = new DeferredObject<List<DiningMenu>, Exception, Void>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Object>() {
            
            @Override
            public void onDone(Object o) {
                d.resolve(mNBDiningMenus);
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Get the menu for a specific dining hall.
     * @param location Dining hall to get menu for
     * @return Promise for the dining hall menu
     */
    public static Promise<DiningMenu, Exception, Void> getDiningLocation(@NonNull final String location) {
        final Deferred<DiningMenu, Exception, Void> d = new DeferredObject<DiningMenu, Exception, Void>();
        setup();

        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Object>() {

            @Override
            public void onDone(Object o) {
                // Get the dining hall menu with matching name
                for (DiningMenu diningMenu : mNBDiningMenus) {
                    if (diningMenu.getLocationName().equalsIgnoreCase(location)) {
                        d.resolve(diningMenu);
                        return;
                    }
                }

                // No matching dining hall found
                Log.w(TAG, "Dining hall \"" + location + "\" not found in API.");
                d.reject(new IllegalArgumentException("Dining hall not found"));
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }

}
