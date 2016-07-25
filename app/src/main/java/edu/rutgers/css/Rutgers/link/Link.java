package edu.rutgers.css.Rutgers.link;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * POJO for holding link info
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"enabled", "removable"})
public class Link implements Serializable {
    public enum Schema {
        RUTGERS, HTTP
    }

    private final String handle;
    private final List<String> pathParts;
    private final String title;
    private final boolean removable;
    private boolean enabled;

    public Link(Link other) {
        this.handle = other.handle;
        this.pathParts = new ArrayList<>(other.pathParts);
        this.title = other.title;
        this.removable = other.removable;
        this.enabled = other.enabled;
    }

    public Link(String handle, List<String> pathParts, String title) {
        this.handle = handle;
        this.pathParts = pathParts;
        this.title = title;
        this.removable = true;
        this.enabled = true;
    }

    public static Link createLink(Uri uri, String title) {
        return createLink(uri, title, true);
    }

    public static Link createLink(Uri uri, String title, boolean removable) {
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
                uriBuilder.authority(Config.API_HOSTNAME);
                uriBuilder.appendPath("link");
                uriBuilder.appendPath(handle);
                break;
        }

        for (final String arg : pathParts) {
            uriBuilder.appendPath(arg);
        }

        return uriBuilder.build();
    }
}
