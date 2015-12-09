package edu.rutgers.css.Rutgers.link;

import java.util.List;

import edu.rutgers.css.Rutgers.model.Channel;
import lombok.Data;

/**
 * Args to use when doing the initial load
 */
@Data
public class LinkLoadArgs {
    public final Channel channel;
    public final List<String> pathParts;
}
