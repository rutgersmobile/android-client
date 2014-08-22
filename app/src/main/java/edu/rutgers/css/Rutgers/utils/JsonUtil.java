package edu.rutgers.css.Rutgers.utils;

import org.json.JSONArray;

/**
 * Created by jamchamb on 8/21/14.
 */
public class JsonUtil {

    public static String[] jsonToStringArray(JSONArray strings) {
        final int size = strings.length();
        String[] result = new String[size];

        for(int i = 0; i < size; i++) {
            result[i] = strings.optString(i);
        }

        return result;
    }

}
