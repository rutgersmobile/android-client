package edu.rutgers.css.Rutgers.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.interfaces.ChannelManagerProvider;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.MainScreen;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * RU Mobile main activity
 */
public class MainActivity extends LocationProviderActivity implements
        ChannelManagerProvider {

    /** Log tag */
    private static final String TAG = "MainActivity";

    /* Member data */
    private ChannelManager mChannelManager;
    private ComponentFactory mComponentFactory;
    private ActionBarDrawerToggle mDrawerToggle;
    private RMenuAdapter mDrawerAdapter;
    private boolean mLoadedShortcuts;
    private boolean mTutorialDrawerCheck;

    /* View references */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;

    @Override
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This will create the UUID if one does not yet exist
        Log.d(TAG, "UUID: " + AppUtils.getUUID(this));

        mComponentFactory = new ComponentFactory();
        mChannelManager = new ChannelManager();

        /*
        if (BuildConfig.DEBUG) {
            getSupportFragmentManager().enableDebugLogging(true);
        }
        */

        // Initialize settings, if necessary
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // Check if this is the first time the app is being launched
        if (PrefUtils.isFirstLaunch(this)) {
            Log.i(TAG, "First launch");

            // First launch, create analytics event & show settings screen
            Analytics.queueEvent(this, Analytics.NEW_INSTALL, null);

            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);

            PrefUtils.markFirstLaunch(this);
        }

        // Determine whether to run nav drawer tutorial
        if (PrefUtils.hasDrawerBeenUsed(this)) {
            mTutorialDrawerCheck = false;
        } else {
            mTutorialDrawerCheck = true;
            Log.i(TAG, "Drawer never opened before, show tutorial!");
        }

        // Enable drawer icon
        if (getActionBar() != null) {
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
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mLoadedShortcuts) loadWebShortcuts();
                if (mTutorialDrawerCheck) {
                    PrefUtils.markDrawerUsed(getApplicationContext());
                    mTutorialDrawerCheck = false;
                    Log.i(TAG, "Drawer opened for first time.");
                }
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuRow clickedRow = (RMenuRow) parent.getAdapter().getItem(position);
                if (!(clickedRow instanceof RMenuItemRow)) return;

                Bundle clickedArgs = ((RMenuItemRow) clickedRow).getArgs();
                // This is a top level menu press
                clickedArgs.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);
                
                // Launch component
                switchFragments(clickedArgs);
                
                //mDrawerAdapter.setSelectedPos(position);
                mDrawerListView.invalidateViews();
                mDrawerLayout.closeDrawer(mDrawerListView); // Close menu after a click
            }

        });

        // Load nav drawer items
        mLoadedShortcuts = false;
        loadChannels();
        loadWebShortcuts();
        
        // Display initial fragment
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            fm.beginTransaction()
                .replace(R.id.main_content_frame, new MainScreen(), MainScreen.HANDLE)
                .commit();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Attempt to flush analytics events to server
        Analytics.postEvents(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clear AQuery cache on exit
        if (isTaskRoot()) {
            AQUtility.cleanCacheAsync(this);
        }
    }
    
    @Override
    public void onBackPressed() {
        Log.v(TAG, "Back button pressed. Leaving top component: " + AppUtils.topHandle(this));

        // If drawer is open, intercept back press to close drawer
        if (mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView);
            return;
        }

        // If web display is active, send back button presses to it for navigating browser history
        if (AppUtils.isOnTop(this, WebDisplay.HANDLE)) {
            Fragment webView = getSupportFragmentManager().findFragmentByTag(WebDisplay.HANDLE);
            if (webView != null && webView.isVisible()) {
                if (((WebDisplay) webView).backPress()) {
                    Log.d(TAG, "Triggered WebView back button");
                    return;
                }
            }
        }

        super.onBackPressed();
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
                Bundle aboutArgs = AboutDisplay.createArgs();
                aboutArgs.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);
                switchFragments(aboutArgs);
                mDrawerLayout.closeDrawer(mDrawerListView);
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
                mLoadedShortcuts = true;
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

        for (Channel channel: channels) {
            Bundle itemArgs = new Bundle();
            itemArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
            itemArgs.putString(ComponentFactory.ARG_COMPONENT_TAG, channel.getView());

            if (StringUtils.isNotBlank(channel.getApi())) {
                itemArgs.putString(ComponentFactory.ARG_API_TAG, channel.getApi());
            }

            if (StringUtils.isNotBlank(channel.getUrl())) {
                itemArgs.putString(ComponentFactory.ARG_URL_TAG, channel.getUrl());
            }

            if (channel.getData() != null) {
                itemArgs.putString(ComponentFactory.ARG_DATA_TAG, channel.getData().toString());
            }

            RMenuItemRow newSMI = new RMenuItemRow(itemArgs);
            // Try to find icon for this item and set it
            newSMI.setDrawable(ImageUtils.getIcon(getResources(), channel.getHandle()));

            // Add the item to the drawer
            mDrawerAdapter.add(newSMI);
        }
    }

    /*
     * Fragment display methods
     */

    /**
     * Add current fragment to the backstack and switch to the new one defined by given arguments.
     * For calls from the nav drawer, this will attempt to pop all backstack history until the last
     * time the desired channel was launched.
     * @param args Argument bundle with at least 'component' argument set to describe which
     *             component to build. All other arguments will be passed to the new fragment.
     * @return True if the new fragment was successfully created, false if not.
     */
    public boolean switchFragments(@NonNull Bundle args) {
        final String componentTag = args.getString(ComponentFactory.ARG_COMPONENT_TAG);
        final boolean isTopLevel = args.getBoolean(ComponentFactory.ARG_TOP_LEVEL);

        // Attempt to create the fragment
        final Fragment fragment = mComponentFactory.createFragment(args);
        if (fragment == null) {
            sendChannelErrorEvent(args); // Channel launch failure analytics event
            return false;
        } else {
            sendChannelEvent(args); // Channel launch analytics event
        }

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtils.closeKeyboard(this);

        final FragmentManager fm = getSupportFragmentManager();

        // If this is a top level (nav drawer) press, find the last time this channel was launched
        // and pop backstack to it
        if (isTopLevel && fm.findFragmentByTag(componentTag) != null) {
            fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // Switch the main content fragment
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left,
                        R.animator.slide_in_left, R.animator.slide_out_right)
                .replace(R.id.main_content_frame, fragment, componentTag)
                .addToBackStack(componentTag)
                .commit();

        return true;
    }

    /**
     * Show a dialog fragment and add it to backstack
     * @param dialogFragment Dialog fragment to display
     * @param tag Tag for fragment transaction backstack
     */
    public void showDialogFragment(@NonNull DialogFragment dialogFragment, @NonNull String tag) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(tag);
        dialogFragment.show(ft, tag);
    }

    private void sendChannelEvent(@NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("handle", args.getString(ComponentFactory.ARG_COMPONENT_TAG));
            extras.put("url", args.getString(ComponentFactory.ARG_URL_TAG));
            extras.put("api", args.getString(ComponentFactory.ARG_API_TAG));
            extras.put("title", args.getString(ComponentFactory.ARG_TITLE_TAG));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Analytics.queueEvent(this, Analytics.CHANNEL_OPENED, extras);
    }

    private void sendChannelErrorEvent(@NonNull Bundle args) {
        JSONObject extras = new JSONObject();
        try {
            extras.put("description","failed to open channel");
            extras.put("handle", args.getString(ComponentFactory.ARG_COMPONENT_TAG));
            extras.put("url", args.getString(ComponentFactory.ARG_URL_TAG));
            extras.put("api", args.getString(ComponentFactory.ARG_API_TAG));
            extras.put("title", args.getString(ComponentFactory.ARG_TITLE_TAG));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Analytics.queueEvent(this, Analytics.ERROR, extras);
    }

}
