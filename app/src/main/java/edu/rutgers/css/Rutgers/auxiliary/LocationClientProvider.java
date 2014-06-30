package edu.rutgers.css.Rutgers.auxiliary;

import com.google.android.gms.location.LocationClient;

public interface LocationClientProvider {

	public LocationClient getLocationClient();
	public boolean servicesConnected();
	
}
