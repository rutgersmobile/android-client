package edu.rutgers.css.Rutgers.link;

import android.net.Uri;

import java.io.Serializable;
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

    public Uri getUri(Schema schema) {
        final Uri.Builder uriBuilder = new Uri.Builder();

        switch (schema) {
            case RUTGERS:
                uriBuilder.scheme("rutgers");
                break;
            case HTTP:
                uriBuilder.scheme("http");
                uriBuilder.authority("rumobile.rutgers.edu");
                uriBuilder.appendPath("link");
                break;
        }

        uriBuilder.appendPath(handle);

        for (final String arg : pathParts) {
            uriBuilder.appendPath(arg);
        }

        return uriBuilder.build();
    }
}
