package edu.rutgers.css.Rutgers.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.utils.LocationUtils;

/**
 * Base for an activity that can connect to Google location services and provide location
 * information to child fragments.
 */
public abstract class LocationProviderActivity extends FragmentActivity implements
        LocationClientProvider,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    /* Member data */
    private LocationClient mLocationClient;
    private List<WeakReference<GooglePlayServicesClient.ConnectionCallbacks>> mLocationListeners = new ArrayList<>(5);

    /** True when resolving Google Services error. */
    private boolean mResolvingError = false;

    /**
     * For providing the location client to fragments
     */
    @Override
    public LocationClient getLocationClient() {
        return mLocationClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect to Google Play location services
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to location services when activity becomes visible
        for (WeakReference<GooglePlayServicesClient.ConnectionCallbacks> listener: mLocationListeners) {
            if (listener.get() != null) mLocationClient.registerConnectionCallbacks(listener.get());
        }

        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from location services when activity is no longer visible
        for (WeakReference<GooglePlayServicesClient.ConnectionCallbacks> listener: mLocationListeners) {
            if (listener.get() != null) mLocationClient.unregisterConnectionCallbacks(listener.get());
        }

        mLocationClient.disconnect();
    }

    /**
     * Register a child fragment with the main activity's location client.
     * @param listener Fragment that uses the location client.
     */
    @Override
    public void registerListener(GooglePlayServicesClient.ConnectionCallbacks listener) {
        if (mLocationClient != null) {
            mLocationClient.registerConnectionCallbacks(listener);
            mLocationListeners.add(new WeakReference<>(listener));
            Log.d(Config.APPTAG, "Registered location listener: " + listener.toString());
        } else {
            Log.e(Config.APPTAG, "Location client not set. Failed to register listener: " + listener.toString());
        }
    }

    /**
     * Unregister a child fragment from the main activity's location client.
     * @param listener Play services Connection Callbacks listener
     */
    @Override
    public void unregisterListener(GooglePlayServicesClient.ConnectionCallbacks listener) {
        for (WeakReference<GooglePlayServicesClient.ConnectionCallbacks> curRef: mLocationListeners) {
            if (curRef.get() == listener) {
                mLocationListeners.remove(curRef);
                break;
            }
        }

        if (mLocationClient != null) {
            mLocationClient.unregisterConnectionCallbacks(listener);
            Log.d(Config.APPTAG, "Unregistered location listener: " + listener.toString());
        }
    }

    /**
     * {@inheritDoc}
     * @param connectionHint Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(Config.APPTAG, "Connected to Google Play services.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisconnected() {
        Log.i(Config.APPTAG, "Disconnected from Google Play services");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) return;

        Log.w(Config.APPTAG, "Attempting to resolve Play Services connection failure");

        if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, LocationUtils.REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(Config.APPTAG, Log.getStackTraceString(e));
                mLocationClient.connect(); // Try again
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
                        Log.w(Config.APPTAG, "Connection failure resolved by Google Play");
                        if (!mLocationClient.isConnecting() && !mLocationClient.isConnected()) {
                            Log.w(Config.APPTAG, "Attempting to reconnect to Play Services...");
                            mLocationClient.connect();
                        }
                        break;

                    default:
                        Log.w(Config.APPTAG, "Connection failure not resolved by Google Play (result: "+resultCode+")");
                        break;
                }
                break;

            // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.w(Config.APPTAG, "Unknown request code: " + requestCode);
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
