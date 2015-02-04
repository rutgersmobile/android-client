package edu.rutgers.css.Rutgers.ui;

import android.app.Activity;
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
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.interfaces.ChannelManagerProvider;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.MainScreen;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.LocationUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * RU Mobile main activity
 *
 */
public class MainActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClientProvider, ChannelManagerProvider {

    /* Log tag */
    private static final String TAG = "MainActivity";

    /* Member data */
    private ChannelManager mChannelManager;
    private LocationClient mLocationClient;
    private ActionBarDrawerToggle mDrawerToggle;
    private RMenuAdapter mDrawerAdapter;
    private List<WeakReference<GooglePlayServicesClient.ConnectionCallbacks>> mLocationListeners = new ArrayList<>(5);

    /* View references */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    
    /**
     * For providing the location client to fragments
     */
    @Override
    public LocationClient getLocationClient() {
        return mLocationClient;
    }

    @Override
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This will create the UUID if one does not yet exist
        Log.d(TAG, "UUID: " + AppUtils.getUUID(this));

        // Start Component Factory
        ComponentFactory.getInstance().setMainActivity(this);

        // Set up logo overlay
        //setLogoRootLayoutId(R.id.main_content_frame);

        /*
        if(BuildConfig.DEBUG) {
            getSupportFragmentManager().enableDebugLogging(true);
        }
        */

        // Set default settings the first time the app is run
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // Check if this is the first time the app is being launched
        if(PrefUtils.isFirstLaunch(this)) {
            Log.i(TAG, "First launch");

            // First launch, create analytics event & show settings screen
            Analytics.queueEvent(this, Analytics.NEW_INSTALL, null);

            /*
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            */

            PrefUtils.markFirstLaunch(this);
        }

        // Connect to Google Play location services
        mLocationClient = new LocationClient(this, this, this);

        // Set up channel manager
        mChannelManager = new ChannelManager();

        // Enable drawer icon
        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        // Set up nav drawer
        ArrayList<RMenuRow> menuArray = new ArrayList<>();
        mDrawerAdapter = new RMenuAdapter(this, R.layout.row_drawer_item, R.layout.row_drawer_header, menuArray);
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
                RMenuRow clickedRow = (RMenuRow) parent.getAdapter().getItem(position);
                if(!(clickedRow instanceof RMenuItemRow)) return;

                Bundle clickedArgs = ((RMenuItemRow) clickedRow).getArgs();
                clickedArgs.putBoolean("topLevel", true); // This is a top level menu press
                
                // Launch component
                ComponentFactory.getInstance().switchFragments(clickedArgs);
                
                //mDrawerAdapter.setSelectedPos(position);
                mDrawerListView.invalidateViews();
                mDrawerLayout.closeDrawer(mDrawerListView); // Close menu after a click
            }

        });
        
        // Load nav drawer items
        loadChannels();
        loadWebShortcuts();
        
        // Set up initial fragment
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() == 0) {
            fm.beginTransaction()
                .replace(R.id.main_content_frame, new MainScreen(), MainScreen.HANDLE)
                .commit();
        }

    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Connect to location services when activity becomes visible
        for(WeakReference<GooglePlayServicesClient.ConnectionCallbacks> listener: mLocationListeners) {
            if(listener.get() != null) mLocationClient.registerConnectionCallbacks(listener.get());
        }

        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from location services when activity is no longer visible
        for(WeakReference<GooglePlayServicesClient.ConnectionCallbacks> listener: mLocationListeners) {
            if(listener.get() != null) mLocationClient.unregisterConnectionCallbacks(listener.get());
        }

        mLocationClient.disconnect();

        // Attempt to flush analytics events to server
        Analytics.postEvents(this);
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
        Log.v(TAG, "Back button pressed. Leaving top component: " + AppUtils.topHandle(this));

        // If drawer is open, intercept back press to close drawer
        if(mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView);
            return;
        }

        // If web display is active, send back button presses to it for navigating browser history
        if(AppUtils.isOnTop(this, WebDisplay.HANDLE)) {
            Fragment webView = getSupportFragmentManager().findFragmentByTag(WebDisplay.HANDLE);
            if (webView != null && webView.isVisible()) {
                if(((WebDisplay) webView).backPress()) {
                    Log.d(TAG, "Triggered WebView back button");
                    return;
                }
            }
        }

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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_about:
                ComponentFactory.getInstance().switchFragments(AboutDisplay.createArgs());
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

    /**
     * Register a child fragment with the main activity's location client.
     * @param listener Fragment that uses the location client.
     */
    @Override
    public void registerListener(GooglePlayServicesClient.ConnectionCallbacks listener) {
        mLocationListeners.add(new WeakReference<>(listener));

        if(mLocationClient != null) {
            mLocationClient.registerConnectionCallbacks(listener);
            Log.d(TAG, "Registered location listener: " + listener.toString());
        } else {
            Log.w(TAG, "Failed to register listener: " + listener.toString());
        }
    }

    /**
     * Unregister a child fragment from the main activity's location client.
     * @param listener Play services Connection Callbacks listener
     */
    @Override
    public void unregisterListener(GooglePlayServicesClient.ConnectionCallbacks listener) {
        for(WeakReference<GooglePlayServicesClient.ConnectionCallbacks> curRef: mLocationListeners) {
            if(curRef.get() == listener) {
                mLocationListeners.remove(curRef);
                break;
            }
        }

        if(mLocationClient != null) {
            mLocationClient.unregisterConnectionCallbacks(listener);
            Log.d(TAG, "Unregistered location listener: " + listener.toString());
        }
    }

    /**
     * Play services connected
     * @param connectionHint Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to Google Play services.");
    }

    /**
     * Play services disconnected
     */
    @Override
    public void onDisconnected() {
        Log.i(TAG, "Disconnected from Google Play services");
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Attempting to resolve Play Services connection failure");
        if(connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                Log.e(Config.APPTAG, Log.getStackTraceString(e));
            }
        } else {
            LocationUtils.showErrorDialog(this, connectionResult.getErrorCode());
        }
    }

    /**
     * Handle results from Google Play Services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        Log.w(Config.APPTAG, "Connection failure resolved by Google Play");
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        Log.w(Config.APPTAG, "Connection failure not resolved by Google Play ("+resultCode+")");
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

    /*
     * Nav drawer helpers
     */
    
    /**
     * Add native channel items to the menu.
     */
    private void loadChannels() {
        mChannelManager.loadChannelsFromResource(getResources(), R.raw.channels);
        addMenuSection(getString(R.string.drawer_channels), mChannelManager.getChannels("main"));
    }
    
    /**
     * Grab web channel links and add them to the menu.
     */
    private void loadWebShortcuts() {
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Request.apiArray("shortcuts.txt", Request.CACHE_ONE_DAY)).done(new DoneCallback<JSONArray>() {

            @Override
            public void onDone(JSONArray shortcutsArray) {
                mChannelManager.loadChannelsFromJSONArray(shortcutsArray, "shortcuts");
                addMenuSection(getString(R.string.drawer_shortcuts), mChannelManager.getChannels("shortcuts"));
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                Log.e(TAG, "loadWebShortcuts(): " + AppUtils.formatAjaxStatus(status));
            }

        });
        
    }

    /**
     * Add channels to navigation drawer.
     * @param category Section title
     * @param channels Channels to add to nav drawer
     */
    private void addMenuSection(String category, List<Channel> channels) {
        //mDrawerAdapter.add(new RMenuHeaderRow(category))

        final String homeCampus = RutgersUtils.getHomeCampus(this);

        for(Channel channel: channels) {
            Bundle itemArgs = new Bundle();
            itemArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
            itemArgs.putString(ComponentFactory.ARG_COMPONENT_TAG, channel.getView());
            if(!StringUtils.isBlank(channel.getApi())) itemArgs.putString(ComponentFactory.ARG_API_TAG, channel.getApi());
            if(!StringUtils.isBlank(channel.getUrl())) itemArgs.putString(ComponentFactory.ARG_URL_TAG, channel.getUrl());
            if(channel.getData() != null) itemArgs.putString(ComponentFactory.ARG_DATA_TAG, channel.getData().toString());

            RMenuItemRow newSMI = new RMenuItemRow(itemArgs);
            // Try to find icon for this item and set it
            newSMI.setDrawable(ImageUtils.getIcon(getResources(), channel.getHandle()));

            // Add the item to the drawer
            mDrawerAdapter.add(newSMI);
        }
    }

}
