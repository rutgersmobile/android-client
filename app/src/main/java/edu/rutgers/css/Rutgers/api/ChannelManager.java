package edu.rutgers.css.Rutgers.api;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Maintains the list of loaded RU Mobile channels.
 */
public final class ChannelManager {

    private static final String TAG = "ChannelManager";

    private Map<String, Channel> channelsMap;

    public ChannelManager() {
        channelsMap = Collections.synchronizedMap(new LinkedHashMap<String, Channel>());
    }

    /**
     * Get all channels
     * @return JSON array of all channel objects
     */
    public List<Channel> getChannels() {
        ArrayList<Channel> result = new ArrayList<>();

        Set<Map.Entry<String, Channel>> set = channelsMap.entrySet();
        for (Map.Entry<String, Channel> entry: set) {
            result.add(entry.getValue());
        }

        return result;
    }

    /**
     * Get channels from a specific channel category (channels, shortcuts)
     * @param category Category name
     * @return JSON array of all channel objects in category
     */
    public List<Channel> getChannels(@NonNull String category) {
        ArrayList<Channel> result = new ArrayList<>();

        Set<Map.Entry<String, Channel>> set = channelsMap.entrySet();
        for (Map.Entry<String, Channel> entry: set) {
            Channel curChannel = entry.getValue();
            if (category.equalsIgnoreCase(curChannel.getCategory())) result.add(curChannel);
        }

        return result;
    }

    /**
     * Load channel data from JSON Array
     * @param array Channel data JSON Array
     */
    public void loadChannelsFromJSONArray(@NonNull JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject cur = array.getJSONObject(i);
                addChannel(cur);
            } catch (JSONException e) {
                LOGW(TAG, "loadChannelsFromJSONArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Load channel data from JSON Array, setting category for all items
     * @param array Channel data JSON Array
     * @param category Category name
     */
    public void loadChannelsFromJSONArray(@NonNull JSONArray array, @NonNull String category) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject cur = array.getJSONObject(i);
                cur.putOpt("category", category);
                addChannel(cur);
            } catch (JSONException e) {
                LOGW(TAG, "loadChannelsFromJSONArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Load channel data JSON Array from a raw resource file
     * @param resources App Resources object
     * @param resourceId Raw resource file ID
     */
    public void loadChannelsFromResource(Resources resources, int resourceId) {
        JSONArray jsonArray = AppUtils.loadRawJSONArray(resources, resourceId);
        loadChannelsFromJSONArray(jsonArray);
    }

    /**
     * Attempt to add a channel. If a mapping for the channel handle already exists, the
     * new channel must have canOverride set to true in order to replace it.
     * @param channelJson Channel JSON
     */
    private void addChannel(@NonNull JSONObject channelJson) {
        try {
            Channel channel = new Channel(channelJson);

            // See if there is already a channel mapped to this handle
            final String handle = channel.getHandle();
            if (channelsMap.get(handle) != null) {
                // If the new channel has override permission, insert it over the old one
                if (channel.canOverride()) {
                    LOGI(TAG, "Overriding channel " + handle);
                    channelsMap.put(handle, channel);
                } else {
                    LOGI(TAG, "Discarding duplicate channel: " + handle);
                }
            } else {
                // This channel is not already mapped - add it in
                channelsMap.put(handle, channel);
            }
        } catch (JSONException e) {
            LOGE(TAG, "Could not add channel: " + e.getMessage());
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
