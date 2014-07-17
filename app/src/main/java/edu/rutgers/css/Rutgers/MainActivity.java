package edu.rutgers.css.Rutgers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.auxiliary.LocationClientProvider;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuHeader;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers.fragments.MainScreen;
import edu.rutgers.css.Rutgers.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.location.LocationUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * RU Mobile main activity
 *
 */
public class MainActivity extends FragmentActivity  implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationClientProvider {
	
	private static final String TAG = "MainActivity";
	private static final String SC_API = "https://rumobile.rutgers.edu/1/shortcuts.txt";

    private ChannelManager mChannelManager;
	private LocationClient mLocationClient;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private RMenuAdapter mDrawerAdapter;
	
	/**
	 * For providing the location client to fragments
	 */
	@Override
	public LocationClient getLocationClient() {
		return mLocationClient;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Log.d(TAG, "UUID: " + AppUtil.getUUID(this));
		
		/*
		 * Set default settings the first time the app is run
		 */
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		
		/*
		 * Connect to Google Play location services
		 */
		mLocationClient = new LocationClient(this, this, this);

        /*
         * Set up channel manager
         */
        mChannelManager = new ChannelManager();

		/*
		 * Set up nav drawer
		 */
		//Enable drawer icon
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        ArrayList<RMenuPart> menuArray = new ArrayList<RMenuPart>();
        mDrawerAdapter = new RMenuAdapter(this, R.layout.main_drawer_item, R.layout.main_drawer_header, menuArray);        
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        
        mDrawerToggle = new ActionBarDrawerToggle(        
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(view.isEnabled() == false) return;
				
				SlideMenuItem clickedItem = (SlideMenuItem) parent.getAdapter().getItem(position);
				if(clickedItem == null) {
					Log.e("SlidingMenu", "Failed sliding menu click, index " + position);
					mDrawerLayout.closeDrawer(mDrawerListView);
					return;
				}
				
				Bundle clickedArgs = clickedItem.getArgs();
				clickedArgs.putBoolean("topLevel", true); // This is a top level menu press
				
				// Launch component
				ComponentFactory.getInstance().switchFragments(clickedArgs);
				
				//mDrawerAdapter.setSelectedPos(position);
				mDrawerListView.invalidateViews();
				mDrawerLayout.closeDrawer(mDrawerListView); // Close menu after a click
			}
        	
        });
        
        /*
         * Load nav drawer items
         */

        // Set up channel items in drawer
        loadChannels();
        
        // Set up web shortcut items in drawer
        loadWebShortcuts();
        
        /*
         * Set up main screen
         */
        ComponentFactory.getInstance().mMainActivity = this;
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
		//contentFrame.removeAllViews();
		
		FragmentManager fm = getSupportFragmentManager();
		if(fm.getBackStackEntryCount() == 0) {
			fm.beginTransaction()
				.replace(R.id.main_content_frame, new MainScreen(), "mainfrag")
				.commit();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Connect to location services when activity becomes visible
		mLocationClient.connect();
	}
	
	@Override
	protected void onStop() {
		// Disconnect from location services when activity is no longer visible
		mLocationClient.disconnect();
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Clear AQuery cache on exit
		if(isTaskRoot()) {
			AQUtility.cleanCacheAsync(this);
		}
	}
	
	@Override
	public void onBackPressed() {
        // If drawer is open, intercept back press to close drawer
        if(mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView);
            return;
        }

		// If web display is active, send back button presses to it for navigating browser history
		Fragment webView = getSupportFragmentManager().findFragmentByTag("www");
		if (webView != null && webView.isVisible()) {
			if(((WebDisplay) webView).backPress()) return;
		}

        // Default back press behavior (go back in fragments, etc.)
		super.onBackPressed();
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    //TODO Save state
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    }
    
    //TODO Restore state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // See if ActionBarDrawerToggle will handle event
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
        	return true;
        }
        
    	// Handle event here or pass it on
        switch(item.getItemId()) {
        
        	// Start the Settings activity
	        case R.id.action_settings:
	        	Intent intent = new Intent(this, SettingsActivity.class);
	        	startActivity(intent);
	        	return true;
	        
	        default:
	        	return super.onOptionsItemSelected(item);
	        	
        }
        
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*
	 * Location service calls
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		Log.v(AppUtil.APPTAG, "Connected to Google Play services");
		servicesConnected();
        /*
        if(mLocationClient != null) {
			// Mock location testing
			//mLocationClient.setMockMode(false);
			Location currentLocation = mLocationClient.getLastLocation();
			if(currentLocation != null) {
				Log.d(AppUtil.APPTAG, currentLocation.toString());
			}
		}
		*/
	}
	
	@Override
	public void onDisconnected() {
		Log.v(AppUtil.APPTAG, "Disconnected from Google Play services");
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if(connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (SendIntentException e) {
				Log.e(AppUtil.APPTAG, Log.getStackTraceString(e));
			}
		}
		else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0);
            if (dialog != null) {
                LocationUtils.ErrorDialogFragment errorFragment = new LocationUtils.ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), AppUtil.APPTAG);
            }
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        Log.d(AppUtil.APPTAG, "resolved by google play");
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        Log.d(AppUtil.APPTAG, "not resolved by google play");
                        break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(AppUtil.APPTAG, "unknown request code " + requestCode);
               break;
        }
    }

    @Override
    public boolean servicesConnected() {
        return LocationUtils.servicesConnected(this);
    }

	/*
	 * Nav drawer helpers
	 */
	
	/**
	 * Add native channel items to the menu.
	 */
	private void loadChannels() {
        mChannelManager.loadChannelsFromResource(getResources(), R.raw.channels);
        addMenuSection(getResources().getString(R.string.drawer_channels), mChannelManager.getChannels("main"));
	}
	
	/**
	 * Grab web links and add them to the menu.
	 */
	private void loadWebShortcuts() {
		Request.jsonArray(SC_API, Request.EXPIRE_ONE_HOUR).done(new AndroidDoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray shortcutsArray) {
				mChannelManager.loadChannelsFromJSONArray(shortcutsArray, "shortcuts");
                addMenuSection(getResources().getString(R.string.drawer_shortcuts), mChannelManager.getChannels("shortcuts"));
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				Log.e(TAG, "loadWebShortcuts(): " + AppUtil.formatAjaxStatus(status));
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
		
	}

    /**
     * Create section header and load menu items from JSON.
     * @param category Section title
     * @param items Menu items JSON
     */
    private void addMenuSection(String category, JSONArray items) {
        mDrawerAdapter.add(new SlideMenuHeader(category));
        for(int i = 0; i < items.length(); i++) {
            try {
                // Create menu item
                JSONObject cur = items.getJSONObject(i);
                Bundle itemArgs = new Bundle();

                // Set title - may be a multi-title object
                itemArgs.putString("title", AppUtil.getLocalTitle(this, cur.get("title")));

                // Set component to launch. Default to WWW for web shortcuts
                if(cur.optString("view").isEmpty() && !cur.optString("url").isEmpty()) {
                    itemArgs.putString("component", "www");
                }
                else {
                    itemArgs.putString("component", cur.getString("view"));
                }

                // Set URL if available
                if(!cur.optString("url").isEmpty()) itemArgs.putString("url", cur.getString("url"));

                // Set API if available
                if(!cur.optString("api").isEmpty()) itemArgs.putString("api", cur.getString("api"));

                // Set data (JSON Array) if available
                if(cur.optJSONArray("data") != null) itemArgs.putString("data", cur.getJSONArray("data").toString());

                SlideMenuItem newSMI = new SlideMenuItem(itemArgs);
                // Try to find icon for this item and set it
                if(!cur.optString("handle").isEmpty()) {
                    newSMI.setDrawable(AppUtil.getIcon(getResources(), cur.getString("handle")));
                }

                // Add the item to the drawer
                mDrawerAdapter.add(newSMI);
            } catch (JSONException e) {
                Log.w(TAG, "loadChannels(): " + e.getMessage());
            }
        }
    }

}
