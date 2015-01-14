package edu.rutgers.css.Rutgers.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;

/**
 * About RUMobile display.
 */
public class AboutActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AboutDisplay())
                .commit();
    }

}
