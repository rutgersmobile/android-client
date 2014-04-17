package edu.rutgers.css.Rutgers;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.util.AQUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

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
		
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("myRutgers");
		ar.add("Sakai");
		ArrayAdapter<String> a = new ArrayAdapter<String>(this, R.layout.main_drawer_item, ar);
		
		list.setAdapter(a);
		
		FragmentManager fm = MainActivity.this.getSupportFragmentManager();

		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.main_content_frame);
		contentFrame.removeAllViews();
		
		Bundle args = new Bundle();
		
		args.putString("title", "News");
		args.putString("component", "dtable");
		args.putString("data", "[{\"title\":\"Rutgers Today\", \"rss\":\"http://news.rutgers.edu/rss/today\"}, {\"title\": \"Newark News\", \"rss\":\"http://news.rutgers.edu/rss/newark\"}, {\"title\": \"Camden News\", \"rss\":\"http://news.rutgers.edu/rss/camden\"}]");
		//args.putString("api", "test");
		 
		
		//args.putString("component", "bus");
		
		/* didn't help
		AQUtility.setExceptionHandler(new UncaughtExceptionHandler() {
	        @Override
	        public void uncaughtException(Thread thread, Throwable ex) {
	               Log.e("AQUtility", ex.getMessage());
	        }
		});
		*/
		
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
