package edu.rutgers.css.Rutgers.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * Combine multiple JSON objects.
     * Duplicate fields will be overwritten, so this is mainly for hashtable-like JSON objects.
     * @param objects JSON objects to combine
     * @return Single JSON object containing the fields of all the combined JSON objects
     * @throws JSONException
     */
    public static JSONObject combineJSONObjs(JSONObject... objects) throws JSONException {
        JSONObject result = new JSONObject();

        for(JSONObject curObj: objects) {
            Iterator<String> keys = curObj.keys();
            while(keys.hasNext()) {
                String curKey = keys.next();
                Object curVal = curObj.get(curKey);
                result.put(curKey, curVal);
            }
        }

        return result;
    }

}
