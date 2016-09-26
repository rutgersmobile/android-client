package edu.rutgers.css.Rutgers.ui;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.model.Motd;
import edu.rutgers.css.Rutgers.model.MotdAPI;
import edu.rutgers.css.Rutgers.model.SimpleAsyncLoader;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Loader for the initial start of the app
 */
public class MainActivityLoader extends SimpleAsyncLoader<MainActivityLoader.InitLoadHolder> {
    public static final String TAG = "MainActivityLoader";

    public MainActivityLoader(Context context) {
        super(context);
    }

    public class InitLoadHolder {
        private final JsonArray array;
        private final Motd motd;

        public InitLoadHolder(final JsonArray array, final Motd motd) {
            this.array = array;
            this.motd = motd;
        }

        public JsonArray getArray() {
            return array;
        }

        public Motd getMotd() {
            return motd;
        }
    }

    @Override
    public InitLoadHolder loadInBackground() {
        JsonArray array;
        Motd motd = null;

        try {
            array = ApiRequest.api("ordered_content.json", TimeUnit.DAYS, JsonArray.class);
        } catch (JsonSyntaxException | IOException e) {
            array = AppUtils.loadRawJSONArray(getContext().getResources(), R.raw.channels);
        }

        try {
            motd = MotdAPI.getMotd();
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
        }

        return new InitLoadHolder(array, motd);
    }
}
