package edu.rutgers.css.Rutgers.link;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * POJO for holding link info
 */
@Data
public class Link implements Serializable {
    public enum Schema {
        RUTGERS, HTTP
    }

    private final String handle;
    private final List<String> pathParts;
    private final String title;

    public static Link createLink(Uri uri, String title) {
        if (uri.getScheme().equals("rutgers")) {
            final String handle = uri.getAuthority();
            final List<String> pathParts = uri.getPathSegments();
            return new Link(handle, pathParts, title);
        } else if (uri.getScheme().equals("http") && uri.getPathSegments().size() > 1) {
            final List<String> pathParts = new ArrayList<>(uri.getPathSegments());
            pathParts.remove(0);
            final String handle = pathParts.remove(0);
            return new Link(handle, pathParts, title);
        }

        return null;
    }

    public Uri getUri(Schema schema) {
        final Uri.Builder uriBuilder = new Uri.Builder();

        switch (schema) {
            case RUTGERS:
                uriBuilder.scheme("rutgers");
                uriBuilder.authority(handle);
                break;
            case HTTP:
                uriBuilder.scheme("http");
                uriBuilder.authority("rumobile.rutgers.edu");
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
