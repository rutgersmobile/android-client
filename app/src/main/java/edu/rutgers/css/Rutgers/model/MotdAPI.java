package edu.rutgers.css.Rutgers.model;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.api.ApiRequest;

/**
 * Class for getting the message of the day from the Rutgers API
 */
public final class MotdAPI {
    private MotdAPI() {}

    private static final String RESOURCE = "motd.txt";

    public static Promise<Motd, AjaxStatus, Double> getMotd() {
        final AndroidDeferredManager dm = new AndroidDeferredManager();
        final Deferred<Motd, AjaxStatus, Double> df = new DeferredObject<>();
        dm.when(ApiRequest.api(RESOURCE, ApiRequest.CACHE_NEVER)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                Gson gson = new Gson();
                Motd motd = gson.fromJson(result.toString(), Motd.class);
                df.resolve(motd);
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                df.reject(result);
            }
        });
        return df.promise();
    }
}
