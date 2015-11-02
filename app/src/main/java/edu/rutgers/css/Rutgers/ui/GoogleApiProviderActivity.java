package edu.rutgers.css.Rutgers.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.interfaces.GoogleApiClientProvider;
import edu.rutgers.css.Rutgers.utils.LocationUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * Base for an activity that can connect to Google location services and provide location
 * information to child fragments.
 */
public abstract class GoogleApiProviderActivity extends AppCompatActivity implements
        GoogleApiClientProvider,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /* Member data */
    private GoogleApiClient googleApiClient;
    private List<WeakReference<GoogleApiClient.ConnectionCallbacks>> mLocationListeners = new ArrayList<>(5);

    /** True when resolving Google Services error. */
    private boolean mResolvingError = false;

    /**
     * For providing the location client to fragments
     */
    @Override
    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect to Google Play location services
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to location services when activity becomes visible
        for (WeakReference<GoogleApiClient.ConnectionCallbacks> listener: mLocationListeners) {
            if (listener.get() != null) googleApiClient.registerConnectionCallbacks(listener.get());
        }

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from location services when activity is no longer visible
        for (WeakReference<GoogleApiClient.ConnectionCallbacks> listener: mLocationListeners) {
            if (listener.get() != null) googleApiClient.unregisterConnectionCallbacks(listener.get());
        }

        googleApiClient.disconnect();
    }

    /**
     * Register a child fragment with the main activity's location client.
     * @param listener Fragment that uses the location client.
     */
    @Override
    public void registerListener(GoogleApiClient.ConnectionCallbacks listener) {
        if (googleApiClient != null) {
            googleApiClient.registerConnectionCallbacks(listener);
            mLocationListeners.add(new WeakReference<>(listener));
            LOGD(Config.APPTAG, "Registered location listener: " + listener.toString());
        } else {
            LOGE(Config.APPTAG, "Location client not set. Failed to register listener: " + listener.toString());
        }
    }

    /**
     * Unregister a child fragment from the main activity's location client.
     * @param listener Play services Connection Callbacks listener
     */
    @Override
    public void unregisterListener(GoogleApiClient.ConnectionCallbacks listener) {
        for (WeakReference<GoogleApiClient.ConnectionCallbacks> curRef: mLocationListeners) {
            if (curRef.get() == listener) {
                mLocationListeners.remove(curRef);
                break;
            }
        }

        if (googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(listener);
            LOGD(Config.APPTAG, "Unregistered location listener: " + listener.toString());
        }
    }

    /**
     * {@inheritDoc}
     * @param connectionHint Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LOGI(Config.APPTAG, "Connected to Google Play services.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionSuspended(int cause) {
        LOGI(Config.APPTAG, "Suspended Google Play Services Connection for cause: " + cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) return;

        LOGW(Config.APPTAG, "Attempting to resolve Play Services connection failure");

        if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, LocationUtils.REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                LOGE(Config.APPTAG, Log.getStackTraceString(e));
                googleApiClient.connect(); // Try again
            }
        } else {
            LocationUtils.showErrorDialog(this, result.getErrorCode());
            mResolvingError = true;
        }
    }

    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /**
     * Handle results from Google Play Services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {
            case LocationUtils.REQUEST_RESOLVE_ERROR:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LOGW(Config.APPTAG, "Connection failure resolved by Google Play");
                        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                            LOGW(Config.APPTAG, "Attempting to reconnect to Play Services...");
                            googleApiClient.connect();
                        }
                        break;

                    default:
                        LOGW(Config.APPTAG, "Connection failure not resolved by Google Play (result: "+resultCode+")");
                        break;
                }
                break;

            // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                LOGW(Config.APPTAG, "Unknown request code: " + requestCode);
                break;
        }
    }

    /**
     * Check if Google Play Services are connected.
     * @return True if connected, false if not.
     */
    @Override
    public boolean servicesConnected() {
        return LocationUtils.servicesConnected(this);
    }

}
