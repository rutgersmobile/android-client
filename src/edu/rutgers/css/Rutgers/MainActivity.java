package edu.rutgers.css.Rutgers;

import java.util.ArrayList;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuHeader;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers.fragments.DTable;
import edu.rutgers.css.Rutgers2.R;

public class MainActivity extends FragmentActivity {
	
	private static final String TAG = "MainActivity";
	private static final String SC_API = "https://rumobile.rutgers.edu/1/shortcuts.txt";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private RMenuAdapter mDrawerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);				
		
        // Sliding menu setup native items
        ArrayList<RMenuPart> menuArray = new ArrayList<RMenuPart>();
        mDrawerAdapter = new RMenuAdapter(this, R.layout.main_drawer_item, R.layout.main_drawer_header, menuArray);
        
        menuArray.add(new SlideMenuHeader("Channels"));
        menuArray.add(new SlideMenuItem("Bus", "bus"));
        menuArray.add(new SlideMenuItem("News", "dtable", "https://rumobile.rutgers.edu/1/news.txt"));
        menuArray.add(new SlideMenuItem("Food", "food"));
        menuArray.add(new SlideMenuItem("Places", "places"));
        menuArray.add(new SlideMenuItem("Recreation", "dtable", "https://rumobile.rutgers.edu/1/rec.txt"));
        menuArray.add(new SlideMenuItem("Events*", "reader", "http://ruevents.rutgers.edu/events/getEventsRss.xml"));
        
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
				
				mDrawerLayout.closeDrawer(mDrawerList); // Close menu after a click
			}
        	
        });
        	
		FragmentManager fm = MainActivity.this.getSupportFragmentManager();

		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
		contentFrame.removeAllViews();
		
		/* Default to Food screen until main screen is made */
		Bundle args = new Bundle();
		args.putString("title", "Food");
		args.putString("component",  "food");

		Fragment fragment = ComponentFactory.getInstance().createFragment(args);		
		fm.beginTransaction()
			.replace(R.id.main_content_frame, fragment)
			.commit(); 
		
		ComponentFactory.getInstance().mMainActivity = this; 

		/* This loads list of native channels (not complete) */
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
        	Log.d(TAG,"");
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
	
	private class MainListAdapter extends ArrayAdapter<String> {
		public MainListAdapter (ArrayList<String> items) {
			super(MainActivity.this, 0, items);
		}
		
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			if (convertView == null) convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.main_drawer_item, parent);
			
			String title = getItem(position);
			TextView titleTextView = (TextView) convertView.findViewById(R.id.title);
			titleTextView.setText(title);
			
			return convertView;
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
