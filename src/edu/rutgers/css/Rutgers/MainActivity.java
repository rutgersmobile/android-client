package edu.rutgers.css.Rutgers;

import java.util.ArrayList;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.auxiliary.Prediction;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers2.R;

public class MainActivity extends FragmentActivity {
	
	private JSONObject channels;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		Log.d("MainActivity", "oncreate");
		
		setContentView(R.layout.activity_main);
		
		SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setBehindOffset(200);
        menu.setMenu(R.layout.menu);
		
        //false to disable <back arrow on title bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        ListView list = (ListView) menu.getMenu().findViewById(R.id.left_menu);

        // Sliding menu setup
        ArrayList<SlideMenuItem> menuArray = new ArrayList<SlideMenuItem>();
        
        // Menu items with optional/special arguments can be initialized with a custom bundle
        Bundle newsMenuItem = new Bundle();
        newsMenuItem.putString("title", "News");
        newsMenuItem.putString("component", "dtable");
        newsMenuItem.putString("url", "https://rumobile.rutgers.edu/1/news.txt");
        
        Bundle sakaiItem = new Bundle();
        sakaiItem.putString("title", "Sakai");
        sakaiItem.putString("component", "browser");
        sakaiItem.putString("url", "http://sakai.rutgers.edu/");

        Bundle myRUItem = new Bundle();
        myRUItem.putString("title", "myRutgers");
        myRUItem.putString("component", "browser");
        myRUItem.putString("url", "http://my.rutgers.edu/");
        
        // Menu items that only need a title and component name can be initialized with two strings
        menuArray.add(new SlideMenuItem("Bus", "bus"));
        menuArray.add(new SlideMenuItem(newsMenuItem));
        menuArray.add(new SlideMenuItem("Food", "food"));
        menuArray.add(new SlideMenuItem(myRUItem));
        menuArray.add(new SlideMenuItem(sakaiItem));
        
        ArrayAdapter<SlideMenuItem> menuAdapter = new ArrayAdapter<SlideMenuItem>(this, R.layout.main_drawer_item, menuArray);
        
        /*
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("myRutgers");
		ar.add("Sakai");
		ArrayAdapter<String> a = new ArrayAdapter<String>(this, R.layout.main_drawer_item, ar);
		
		list.setAdapter(a);
		*/
		
        list.setAdapter(menuAdapter);
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SlideMenuItem clickedItem = (SlideMenuItem) parent.getAdapter().getItem(position);
				if(clickedItem == null) {
					Log.e("SlidingMenu", "Failed sliding menu click, index " + position);
					return;
				}
				
				Bundle clickedArgs = clickedItem.getArgs();
				
				// Launch browser
				if(clickedArgs.getString("component").equalsIgnoreCase("browser"))	{
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
			}
        	
        });
		
		FragmentManager fm = MainActivity.this.getSupportFragmentManager();

		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
		contentFrame.removeAllViews();
		
		Bundle args = new Bundle();
		
		/*
		args.putString("title", "News");
		args.putString("component", "dtable");
		args.putString("url", "https://rumobile.rutgers.edu/1/news.txt");
		*/
		/*
		args.putString(
				"data", "[" +
						"{\"title\":\"Rutgers Today\", \"channel\":{\"view\":\"Reader\",\"title\":\"Rutgers Today\", \"url\":\"http://news.rutgers.edu/rss/today\"}}," +
						"{\"title\": \"Newark News\", \"channel\":{\"view\":\"Reader\",\"title\": \"Newark News\",\"url\":\"http://news.rutgers.edu/rss/newark\"}}, " +
						"{\"title\": \"Camden News\", \"channel\":{\"view\":\"Reader\",\"title\": \"Camden News\",\"url\":\"http://news.rutgers.edu/rss/camden\"}}, " +
						"{\"title\": \"Rutgers Events\", \"channel\":{\"view\":\"Reader\",\"title\": \"Rutgers Events\",\"url\": \"http://ruevents.rutgers.edu/events/getEventsRss.xml\"}}" +
						"]"
		);
		*/

		//args.putString("api", "test");
		 
		//args.putString("component", "bus");
		
		
		args.putString("title", "Dining");
		args.putString("component",  "food");

		
		Request.api("app").done(new DoneCallback<JSONObject>() {
			public void onDone(JSONObject result) {
				Log.d("MainActivity", "got app data " + result.toString());
			}
		});
		
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
		
		Fragment fragment = ComponentFactory.getInstance().createFragment(args);
		
		fm.beginTransaction()
			.replace(R.id.main_content_frame, fragment)
			.commit(); 
		
		ComponentFactory.getInstance().mMainActivity = this; 

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
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.main_drawer_title);
			
			titleTextView.setText(title);
			
			return convertView;
		}
	}

}
