package edu.rutgers.css.Rutgers.link;

import android.support.v7.widget.ShareActionProvider;

/**
 * Implemented by fragments that can be linked to
 */
public interface Linkable {
    Link getLink();
    String getLinkTitle();
    void setShareIntent();
    ShareActionProvider getShareActionProvider();
}
