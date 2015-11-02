package edu.rutgers.css.Rutgers.interfaces;

import com.google.android.gms.common.api.GoogleApiClient;

public interface GoogleApiClientProvider {
    GoogleApiClient getGoogleApiClient();
    boolean servicesConnected();
    void registerListener(GoogleApiClient.ConnectionCallbacks listener);
    void unregisterListener(GoogleApiClient.ConnectionCallbacks listener);
}
