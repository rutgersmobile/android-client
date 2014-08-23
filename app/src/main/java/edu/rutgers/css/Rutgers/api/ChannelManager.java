package edu.rutgers.css.Rutgers.api;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * ChannelManager
 * Created by jamchamb on 7/1/14.
 *
 */
public class ChannelManager {

    private static final String TAG = "ChannelManager";

    private Map<String, JSONObject> channelsMap;

    public ChannelManager() {
        channelsMap = Collections.synchronizedMap(new LinkedHashMap<String, JSONObject>());
    }

    /**
     * Get all channels
     * @return JSON array of all channel objects
     */
    public JSONArray getChannels() {
        JSONArray result = new JSONArray();
        Set<Map.Entry<String, JSONObject>> set = channelsMap.entrySet();
        for(Map.Entry<String, JSONObject> entry: set) {
            result.put(entry.getValue());
        }
        return result;
    }

    /**
     * Get channels from a specific channel category (channels, shortcuts)
     * @param category Category name
     * @return JSON array of all channel objects in category
     */
    public JSONArray getChannels(String category) {
        JSONArray result = new JSONArray();
        Set<Map.Entry<String, JSONObject>> set = channelsMap.entrySet();
        for(Map.Entry<String, JSONObject> entry: set) {
            JSONObject cur = entry.getValue();
            if(cur.optString("category").equals(category)) result.put(entry.getValue());
        }
        return result;
    }

    /**
     * Load channel data from JSON Array
     * @param array Channel data JSON Array
     */
    public void loadChannelsFromJSONArray(JSONArray array) {
        if(array == null) return;

        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject cur = array.getJSONObject(i);
                addChannel(cur);
            } catch (JSONException e) {
                Log.w(TAG, "loadChannelsFromJSONArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Load channel data from JSON Array, setting category for all items
     * @param array Channel data JSON Array
     * @param category Category name
     */
    public void loadChannelsFromJSONArray(JSONArray array, String category) {
        if(array == null) return;

        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject cur = array.getJSONObject(i);
                cur.putOpt("category", category);
                addChannel(cur);
            } catch (JSONException e) {
                Log.w(TAG, "loadChannelsFromJSONArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Load channel data JSON Array from a raw resource file
     * @param resources App Resources object
     * @param resourceId Raw resource file ID
     */
    public void loadChannelsFromResource(Resources resources, int resourceId) {
        JSONArray jsonArray = AppUtil.loadRawJSONArray(resources, resourceId);
        loadChannelsFromJSONArray(jsonArray);
    }

    /**
     * Attempt to add a channel. If a mapping for the channel handle already exists, the
     * new channel must have canOverride set to true in order to replace it.
     * @param channel Channel JSON
     */
    private void addChannel(JSONObject channel) {
        if(channel == null) return;

        // Get channel handle
        String handle = channel.optString("handle");
        if(handle.isEmpty()) {
            Log.w(TAG, "Channel JSON has no handle: " + channel.toString());
            return;
        }

        // See if there is already a channel mapped to this handle
        if(channelsMap.get(handle) != null) {
            // If the new channel has override permission, insert it over the old one
            if(channel.optBoolean("canOverride")) {
                Log.i(TAG, "Overriding channel " + handle);
                channelsMap.put(handle, channel);
            }
            else {
                Log.i(TAG, "Discarding duplicate channel: " + channel.optString("handle"));
                return;
            }
        }
        // This channel is not already mapped - add it in
        else {
            channelsMap.put(handle, channel);
        }
    }

    /**
     * Remove a channel
     * @param handle Handle of the channel to remove
     */
    private void removeChannel(String handle) {
        channelsMap.remove(handle);
    }

}
