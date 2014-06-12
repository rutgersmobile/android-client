package edu.rutgers.css.Rutgers;

import java.util.ArrayList;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
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

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.auxiliary.LocationClientProvider;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuHeader;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers.fragments.DTable;
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
	
	private LocationClient mLocationClient;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private RMenuAdapter mDrawerAdapter;
		
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
	@Override
	public LocationClient getLocationClient() {
		return mLocationClient;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Connect to Google Play location service
		mLocationClient = new LocationClient(this, this, this);
		
        // Sliding menu setup native items
        ArrayList<RMenuPart> menuArray = new ArrayList<RMenuPart>();
        mDrawerAdapter = new RMenuAdapter(this, R.layout.main_drawer_item, R.layout.main_drawer_header, menuArray);
        mDrawerAdapter.setSelectColor(getResources().getColor(R.color.drawer_selected));
        
        menuArray.add(new SlideMenuHeader("Channels"));
        menuArray.add(new SlideMenuItem("Bus", "bus"));
        menuArray.add(new SlideMenuItem("News", "dtable", "https://rumobile.rutgers.edu/1/news.txt"));
        menuArray.add(new SlideMenuItem("Food", "food"));
        menuArray.add(new SlideMenuItem("Places", "places"));
        menuArray.add(new SlideMenuItem("Recreation", "dtable", "https://rumobile.rutgers.edu/1/rec.txt"));
        menuArray.add(new SlideMenuItem("Events", "reader", "http://ruevents.rutgers.edu/events/getEventsRss.xml"));
        //menuArray.add(new SlideMenuItem("RU Today", "reader", "http://medrel.drupaldev.rutgers.edu/rss/today"));
        
        // Sliding menu set up web shortcuts
        mDrawerAdapter.add(new SlideMenuHeader("Shortcuts"));
        loadWebShortcuts(mDrawerAdapter);
        
		// Set up Navigation Drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
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
        
        //false to disable <back arrow on title bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(view.isEnabled() == false) return;
				
				SlideMenuItem clickedItem = (SlideMenuItem) parent.getAdapter().getItem(position);
				if(clickedItem == null) {
					Log.e("SlidingMenu", "Failed sliding menu click, index " + position);
					mDrawerLayout.closeDrawer(mDrawerList);
					return;
				}
				
				Bundle clickedArgs = clickedItem.getArgs();
				
				// Launch browser
				if(clickedArgs.getString("component").equalsIgnoreCase("www"))	{
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(clickedArgs.getString("url")));
					startActivity(i);
				}
				// Launch channel component
				else {
					FragmentManager fm = MainActivity.this.getSupportFragmentManager();	
					Fragment fragment = ComponentFactory.getInstance().createFragment(clickedArgs);
					if(fragment == null) {
						Log.e("SlidingMenu", "Failed to create component");
						return;
					}
					fm.beginTransaction()
						.replace(R.id.main_content_frame, fragment)
						.commit(); 	
				}
				
				mDrawerAdapter.setSelectedPos(position);
				mDrawerList.invalidateViews();
				mDrawerLayout.closeDrawer(mDrawerList); // Close menu after a click
			}
        	
        });
        	
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
		contentFrame.removeAllViews();
		
		/* Default to Food screen until main screen is made */
		Bundle args = new Bundle();
		args.putString("title", "Food");
		args.putString("component",  "food");

		
		Fragment fragment = ComponentFactory.getInstance().createFragment(args);
		FragmentManager fm = MainActivity.this.getSupportFragmentManager();
		fm.beginTransaction()
			.replace(R.id.main_content_frame, fragment)
			.commit(); 
		
		ComponentFactory.getInstance().mMainActivity = this; 

		/* This loads list of native channels (API is incomplete) */
		/*		
 		Request.api("app").done(new DoneCallback<JSONObject>() {
			public void onDone(JSONObject result) {
				Log.d("MainActivity", "got app data " + result.toString());
				System.out.println("API loaded: " + result.toString());
			}
		});
		*/
		
		/*
		Nextbus.stopPredict("nb", "Hill Center").done(new DoneCallback<ArrayList>() {
		@Override
		public void onDone(ArrayList predictions) {
			for (Object o : predictions) {
				Prediction p = (Prediction) o;
				Log.d("Main", "title: " + p.getTitle() + " direction: " + p.getDirection() + " minutes: " + p.getMinutes());
			}
		}
		}).fail(new FailCallback<Exception>() {
			
			@Override
			public void onFail(Exception e) {
				Log.d("Main", Log.getStackTraceString(e));
			}
		});
		*/

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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
        	//Log.d(TAG,"");
        	return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onConnected(Bundle dataBundle) {
		Log.d(LocationUtils.APPTAG, "Connected to Google Play services");
		servicesConnected();
		if(mLocationClient != null) {
			//mLocationClient.setMockMode(false); // FOR TESTING ONLY
/*			Location currentLocation = mLocationClient.getLastLocation();
			if(currentLocation != null) {
				Log.d(LocationUtils.APPTAG, currentLocation.toString());
			}*/
		}
	}
	
	@Override
	public void onDisconnected() {
		Log.d(LocationUtils.APPTAG, "Disconnected from Google Play services");
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if(connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (SendIntentException e) {
				Log.e(LocationUtils.APPTAG, Log.getStackTraceString(e));
			}
		}
		else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
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
                        Log.d(LocationUtils.APPTAG, "resolved by google play");
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        Log.d(LocationUtils.APPTAG, "not resolved by google play");
                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(LocationUtils.APPTAG, "unknown request code " + requestCode);
               break;
        }
    }
    
	/**
	 * Check if Google Play services is connected.
	 * @return True if connected, false if not.
	 */
	public boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		
		if(resultCode == ConnectionResult.SUCCESS) {
			Log.d(TAG, "Google Play services available.");
			return true;
		}
		else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
		}
	}
	
	/**
	 * Grab web links and add them to the menu.
	 * @param menuArray Array that holds the menu objects
	 */
	private void loadWebShortcuts(final RMenuAdapter menuAdapter) {
		
		Request.jsonArray(SC_API, Request.EXPIRE_ONE_HOUR).done(new DoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray shortcutsArray) {
				
				// Get each shortcut from array and add it to the sliding menu
				for(int i = 0; i < shortcutsArray.length(); i++) {		
					try {
						JSONObject curShortcut = shortcutsArray.getJSONObject(i);
						String title = DTable.getLocalTitle(curShortcut.get("title"));
						String url = curShortcut.getString("url");
						menuAdapter.add(new SlideMenuItem(title, "www", url));
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
						continue;
					}
				}
				
			}
			
		}).fail(new FailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				Log.e(TAG, status.getMessage());
			}
			
		});
		
	}
	
}
