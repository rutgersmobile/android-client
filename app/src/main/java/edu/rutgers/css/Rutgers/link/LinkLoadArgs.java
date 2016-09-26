package edu.rutgers.css.Rutgers.link;

import java.util.List;

import edu.rutgers.css.Rutgers.model.Channel;

/**
 * Args to use when doing the initial load
 */
public class LinkLoadArgs {
    public final Channel channel;
    public final List<String> pathParts;

    public LinkLoadArgs(final Channel channel, final List<String> pathParts) {
        this.channel = channel;
        this.pathParts = pathParts;
    }

    public Channel getChannel() {
        return channel;
    }

    public List<String> getPathParts() {
        return pathParts;
    }
}
