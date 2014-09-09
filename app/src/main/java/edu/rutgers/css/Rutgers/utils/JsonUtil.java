package edu.rutgers.css.Rutgers.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jamchamb on 8/21/14.
 */
public class JsonUtil {

    private static final String TAG = "JsonUtil";

    public static String[] jsonToStringArray(JSONArray strings) {
        final int size = strings.length();
        String[] result = new String[size];

        for(int i = 0; i < size; i++) {
            result[i] = strings.optString(i);
        }

        return result;
    }

    public static JSONObject combineJSONObjs(JSONObject conf1, JSONObject conf2) {
        JSONObject result = new JSONObject();
        ArrayList<JSONObject> confs = new ArrayList<JSONObject>();
        confs.add(conf1);
        confs.add(conf2);

        for(JSONObject curConf: confs) {
            Iterator<String> confKeys = curConf.keys();
            while(confKeys.hasNext()) {
                try {
                    String curKey = confKeys.next();
                    Object curObj = curConf.get(curKey);
                    result.put(curKey, curObj);
                } catch(JSONException e) {
                    Log.e(TAG, "combineJSONObjs(): " + e.getMessage());
                    return null;
                }
            }
        }

        return result;
    }

}
