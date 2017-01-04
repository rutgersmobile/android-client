package edu.rutgers.css.Rutgers.api;

/**
 * Created by mattro on 1/4/17.
 */

public final class APIUtils {
    /**
     * Calculate the distance between to points on the earths surface
     * @param lat0 Latitude of first point in degrees
     * @param lon0 Longitude of first point in degrees
     * @param lat1 Latitude of second point in degrees
     * @param lon1 Longitude of second point in degrees
     * @return Distance between points in meters
     */
    public static double distanceBetween(double lat0, double lon0, double lat1, double lon1) {
        final double dlat = degreesToRadians(lat1 - lat0);
        final double dlon = degreesToRadians(lon1 - lon0);
        final double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
            + Math.cos(degreesToRadians(lat0)) * Math.cos(degreesToRadians(lat1))
            * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c;
    }

    private static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }
}
