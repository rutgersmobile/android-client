package edu.rutgers.css.Rutgers.interfaces;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public interface LocationClientProvider {

	public LocationClient getLocationClient();
    public boolean servicesConnected();
    public void registerListener(GooglePlayServicesClient.ConnectionCallbacks listener);
    public void unregisterListener(GooglePlayServicesClient.ConnectionCallbacks listener);
	
}
