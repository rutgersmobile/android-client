package edu.rutgers.css.Rutgers.utils;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by jamchamb on 8/21/14.
 */
public class JsonUtils {

    private static final String TAG = "JsonUtils";

    /**
     * Check if a JSON string is really empty - do not coerce null value into string "null".
     * @param jsonObject JSON object containing string field
     * @param field Name of the string field
     * @return True if there are no contents for the string, false if there are.
     */
    public static boolean stringIsReallyEmpty(@NonNull JSONObject jsonObject, @NonNull String field) {
        if(jsonObject.isNull(field)) return true;
        else if(jsonObject.optString(field).isEmpty()) return true;
        else return false;
    }

    /**
     * Convert a JSON array of strings to a native array of strings.
     * @param strings JSON array containing only strings
     * @return String array
     */
    public static String[] jsonToStringArray(@NonNull JSONArray strings) {
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
    public static JSONObject combineJSONObjs(@NonNull JSONObject... objects) throws JSONException {
        JSONObject result = new JSONObject();

        for(JSONObject curObj: objects) {
            for(Iterator<String> keys = curObj.keys(); keys.hasNext();) {
                String curKey = keys.next();
                Object curVal = curObj.get(curKey);
                result.put(curKey, curVal);
            }
        }

        return result;
    }

}
