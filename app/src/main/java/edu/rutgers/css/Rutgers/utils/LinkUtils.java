package edu.rutgers.css.Rutgers.utils;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for linking
 */
public final class LinkUtils {
    public enum Schema {
        RUTGERS, HTTP
    }

    public static Uri buildUri(final Schema schema, final String... args) {
        final List<String> argsList = new ArrayList<>();
        for (final String arg : args) {
            if (arg != null) {
                argsList.add(arg);
            }
        }

        final Uri.Builder uriBuilder = new Uri.Builder();

        switch (schema) {
            case RUTGERS:
                uriBuilder.scheme("rutgers");
                if (argsList.size() > 0) {
                    uriBuilder.authority(argsList.remove(0));
                }
                break;
            case HTTP:
                uriBuilder.scheme("http");
                uriBuilder.authority("rumobile.rutgers.edu");
                uriBuilder.appendPath("link");
                break;
        }

        for (final String arg : argsList) {
            uriBuilder.appendPath(arg);
        }

        return uriBuilder.build();
    }
}
