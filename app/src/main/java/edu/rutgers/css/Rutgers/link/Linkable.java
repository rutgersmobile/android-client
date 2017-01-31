package edu.rutgers.css.Rutgers.link;

import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;

/**
 * Implemented by fragments that can be linked to
 */
public interface Linkable {
    Link getLink();
    VarTitle getLinkTitle(String channelHandle);
}
