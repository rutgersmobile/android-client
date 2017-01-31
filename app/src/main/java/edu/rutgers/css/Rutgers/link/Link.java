package edu.rutgers.css.Rutgers.link;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;

/**
 * POJO for holding link info
 */
public class Link implements Serializable {
    public enum Schema {
        RUTGERS, HTTP
    }

    private final String handle;
    private final List<String> pathParts;
    private final VarTitle title;
    private final boolean removable;
    private boolean enabled;

    public Link(Link other) {
        this.handle = other.handle;
        this.pathParts = new ArrayList<>(other.pathParts);
        this.title = other.title;
        this.removable = other.removable;
        this.enabled = other.enabled;
    }

    public Link(String handle, List<String> pathParts, VarTitle title) {
        this.handle = handle;
        this.pathParts = pathParts;
        this.title = title;
        this.removable = true;
        this.enabled = true;
    }

    public Link(final String handle, final List<String> pathParts, final VarTitle title,
                final boolean removable, final boolean enabled) {
        this.handle = handle;
        this.pathParts = pathParts;
        this.title = title;
        this.removable = removable;
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }

        if (otherObj instanceof Link) {
            final Link other = (Link) otherObj;
            return this.handle.equals(other.handle)
                && this.pathParts.equals(other.pathParts)
                && this.title.equals(other.title);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + handle.hashCode();
        hash = hash * 31 + pathParts.hashCode();
        hash = hash * 13 + title.hashCode();
        return hash;
    }

    public static Link createLink(Uri uri, VarTitle title) {
        return createLink(uri, title, true);
    }

    public static Link createLink(Uri uri, VarTitle title, boolean removable) {
        if (uri.getScheme().equals("rutgers")) {
            final String handle = uri.getAuthority();
            final List<String> pathParts = uri.getPathSegments();
            return new Link(handle, pathParts, title, removable, true);
        } else if (uri.getScheme().equals("http") && uri.getPathSegments().size() > 1) {
            final List<String> pathParts = new ArrayList<>(uri.getPathSegments());
            pathParts.remove(0);
            final String handle = pathParts.remove(0);
            return new Link(handle, pathParts, title, removable, true);
        }

        return null;
    }

    public Uri getUri() {
        return getUri(Config.SCHEMA);
    }

    public Uri getUri(Schema schema) {
        final Uri.Builder uriBuilder = new Uri.Builder();

        switch (schema) {
            case RUTGERS:
                uriBuilder.scheme("rutgers");
                uriBuilder.authority(handle);
                break;
            case HTTP:
                uriBuilder.scheme(Config.API_SCHEME);
                uriBuilder.authority(Config.LINK_HOSTNAME);
                uriBuilder.appendPath("link");
                uriBuilder.appendPath(handle);
                break;
        }

        for (final String arg : pathParts) {
            uriBuilder.appendPath(arg);
        }

        return uriBuilder.build();
    }

    public String getHandle() {
        return handle;
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    public String getTitle() {
        return title.getTitle();
    }

    public String getTitle(String homeCampus) {
        return title.getTitle(homeCampus);
    }

    public boolean isRemovable() {
        return removable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
