package edu.rutgers.css.Rutgers;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

public class SingleFragmentActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_fragment);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		
		if (fragment == null) {
			// fragment = createFragment();
			// Create fragment based on what was specified in the savedInstanceState
			
			fragment = ComponentFactory.getInstance().createFragment(savedInstanceState);
			
			fm.beginTransaction()
				.add(R.id.fragment_container, fragment)
				.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single, menu);
		return true;
	}
	
}
