package edu.rutgers.css.Rutgers.api;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * ChannelManager
 * Created by jamchamb on 7/1/14.
 *
 */
public class ChannelManager {

    private static final String TAG = "ChannelManager";

    /*
     * channelKeys holds the list of channel keys in the order they were added, to preserve
     * input order when the channels are going to be displayed.
     * channelsMap maps those keys to the channel JSON objects.
     */
    private ArrayList<String> channelKeys;
    private Hashtable<String, JSONObject> channelsMap;

    public ChannelManager() {
        channelKeys = new ArrayList<String>();
        channelsMap = new Hashtable<String, JSONObject>();
    }

    /**
     * Get all channels
     * @return JSON array of all channels
     */
    public JSONArray getChannels() {
        JSONArray result = new JSONArray();
        for(String key: channelKeys) {
            result.put(channelsMap.get(key));
        }
        return result;
    }

    /**
     * Get channels from a specific channel category (channels, shortcuts)
     * @param category Category name
     * @return
     */
    public JSONArray getChannels(String category) {
        JSONArray result = new JSONArray();
        for(String key: channelKeys) {
            JSONObject cur = channelsMap.get(key);
            if(cur.optString("category").equals(category)) result.put(cur);
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
                Log.i(TAG, "Discarding duplicate channel: " + channel.toString());
                return;
            }
        }
        // This channel is not already mapped - add it in
        else {
            channelsMap.put(handle, channel);
            channelKeys.add(handle);
        }
    }

    /**
     * Remove a channel
     * @param handle Handle of the channel to remove
     */
    private void removeChannel(String handle) {
        channelKeys.remove(handle);
        channelsMap.remove(handle);
    }

}
