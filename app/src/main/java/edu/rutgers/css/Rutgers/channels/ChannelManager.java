package edu.rutgers.css.Rutgers.channels;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
        channelsMap = Collections.synchronizedMap(new LinkedHashMap<>());
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

    public void setChannelsMap(Map<String, Channel> channelsMap) {
        this.channelsMap = channelsMap;
    }

    public Map<String, Channel> getChannelsMap() {
        return channelsMap;
    }

    /**
     *
     * @param key String that represents the Channel's tag
     * @return Channel that matches the key
     */
    public Channel getChannelByTag(@NonNull String key){
        if(!channelsMap.containsKey(key)){
            return null;
        }
        return channelsMap.get(key);
    }


    /**
     * Load channel data from JSON Array
     * @param array Channel data JSON Array
     */
    public void loadChannelsFromJSONArray(@NonNull JsonArray array) {
        for (JsonElement element : array) {
            try {
                JsonObject cur = element.getAsJsonObject();
                addChannel(cur);
            } catch (IllegalStateException e) {
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
        JsonArray jsonArray = AppUtils.loadRawJSONArray(resources, resourceId);
        if (jsonArray != null) {
            loadChannelsFromJSONArray(jsonArray);
        }
    }

    /**
     * Attempt to add a channel. If a mapping for the channel handle already exists, the
     * new channel must have canOverride set to true in order to replace it.
     * @param channelJson Channel JSON
     */
    private void addChannel(@NonNull JsonObject channelJson) {
        try {
            Channel channel = new Channel(channelJson);

            // See if there is already a channel mapped to this handle
            final String handle = channel.getHandle();
            if (channelsMap.get(handle) != null) {
                // If the new channel has override permission, insert it over the old one
                if (channel.getCanOverride()) {
                    LOGI(TAG, "Overriding channel " + handle);
                    channelsMap.put(handle, channel);
                } else {
                    LOGI(TAG, "Discarding duplicate channel: " + handle);
                }
            } else {
                // This channel is not already mapped - add it in
                channelsMap.put(handle, channel);
            }
        } catch (IllegalStateException e) {
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

    public void clear() {
        channelsMap.clear();
    }

}
