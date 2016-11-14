package edu.rutgers.css.Rutgers.interfaces;

import edu.rutgers.css.Rutgers.channels.ChannelManager;

/**
 * Provides a {@link ChannelManager}
 */
public interface ChannelManagerProvider {
    ChannelManager getChannelManager();
}
