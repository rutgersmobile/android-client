package edu.rutgers.css.Rutgers.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.MainScreen;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Main activity. Handles navigation drawer, displayed fragments, and connection to location services.
 */

public class MainActivity extends LocationProviderActivity implements
        ChannelManagerProvider {

    /** Log tag */
    private static final String TAG = "MainActivity";

    /* Member data */
    private ChannelManager mChannelManager;
    private ComponentFactory mComponentFactory;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerAdapter mDrawerAdapter;

    private Toolbar mToolbar;

    /** Flags whether the drawer tutorial _should_ be displayed. */
    private boolean mShowDrawerShowcase;

    /** Flags whether the drawer tutorial _is_ being displayed. */
    private boolean mDisplayingTutorial;

    /* View references */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;

    /* Callback for changed preferences */
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private FragmentManager fm = getSupportFragmentManager();



    @Override
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }


    private class DrawerAdapter extends ArrayAdapter<Channel> {
        private class ViewHolder {
            TextView textView;
            ImageView imageView;
        }

        public DrawerAdapter(List<Channel> objects) {
            super(MainActivity.this, R.layout.row_drawer_item, objects);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Channel channel = getItem(position);
            LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            String homeCampus = RutgersUtils.getHomeCampus(MainActivity.this);
            ViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.row_drawer_item, null);

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.title);
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.textView.setText(channel.getTitle(homeCampus));
            holder.imageView.setImageDrawable(ImageUtils.getIcon(getResources(), channel.getHandle()));

            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        LOGD(TAG, "UUID: " + AppUtils.getUUID(this));
        mComponentFactory = new ComponentFactory();
        mChannelManager = new ChannelManager();

        firstLaunchChecks();

        // Enable drawer icon
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setElevation(10);
        }

        // Set up navigation drawer
        mDrawerAdapter = this.new DrawerAdapter(new ArrayList<Channel>());
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.actbar_new));

        mDrawerToggle = new ActionBarDrawerToggle(        
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {super.onDrawerClosed(view);}

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                LOGE(TAG, "Current checked item on drawer opened at: " + mDrawerListView.getCheckedItemPosition());
                if (mShowDrawerShowcase) {
                    PrefUtils.markDrawerUsed(MainActivity.this);
                    mShowDrawerShowcase = false;
                    LOGI(TAG, "Drawer opened for first time.");
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Channel channel = (Channel) parent.getAdapter().getItem(position);
                Bundle channelArgs = new Bundle();
                String homeCampus = RutgersUtils.getHomeCampus(MainActivity.this);

                channelArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
                channelArgs.putString(ComponentFactory.ARG_COMPONENT_TAG, channel.getView());
                channelArgs.putString(ComponentFactory.ARG_HANDLE_TAG, channel.getHandle());

                if (StringUtils.isNotBlank(channel.getApi())) {
                    channelArgs.putString(ComponentFactory.ARG_API_TAG, channel.getApi());
                }

                if (StringUtils.isNotBlank(channel.getUrl())) {
                    channelArgs.putString(ComponentFactory.ARG_URL_TAG, channel.getUrl());
                }

                if (channel.getData() != null) {
                    channelArgs.putString(ComponentFactory.ARG_DATA_TAG, channel.getData().toString());
                }

                channelArgs.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);


                mDrawerListView.setItemChecked(position, true);
                LOGI(TAG, "Currently checked item position: " + mDrawerListView.getCheckedItemPosition());

                mDrawerListView.invalidateViews();
                // Launch component
                switchDrawerFragments(channelArgs);
                //mDrawerAdapter.setSelectedPos(position);

                mDrawerLayout.closeDrawer(mDrawerListView); // Close menu after a click
            }

        });

        // Reload web channels when we change preferences to update campus-based names
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                mDrawerAdapter.notifyDataSetChanged();
            }
        };

        preferences.registerOnSharedPreferenceChangeListener(listener);

        // Load nav drawer items
        loadChannels();
        
        // Display initial fragment
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
    protected void onResume() {
        super.onResume();

        showDrawerShowcase();
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
        // If drawer is open, intercept back press to close drawer
        if (mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView);
            return;
        }

        // If web display is active, send back button presses to it for navigating browser history
        if (AppUtils.isOnTop(this, WebDisplay.HANDLE)) {
            Fragment webView = fm.findFragmentByTag(WebDisplay.HANDLE);
            if (webView != null && webView.isVisible()) {
                if (((WebDisplay) webView).backPress()) {
                    LOGD(TAG, "Triggered WebView back button");
                    return;
                }
            }
        }
        //To change which menu item is highlighted to the correct currently displayed fragment, listen
        //for when the backstack changes after the back button is pressed and set the view that matches
        //the fragment at the end of the backstack as checked
        fm.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged(){
                        mDrawerListView.setItemChecked(mDrawerListView.getCheckedItemPosition(), false);
                        if(fm.getBackStackEntryCount() > 0) {
                            FragmentManager.BackStackEntry backStackEntry;
                            backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
                            String fragmentTag = backStackEntry.getName();
                            for(Channel channel: mChannelManager.getChannels()){
                                if(channel.getHandle().equalsIgnoreCase((fragmentTag))){
                                    int position = mDrawerAdapter.getPosition(mChannelManager.getChannels().get(k));
                                    mDrawerListView.setItemChecked(position, true);
                                }
                            }
                        }
                    }
                }
        );

        LOGV(TAG, "Back button pressed. Leaving top component: " + AppUtils.topHandle(this));
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
                switchDrawerFragments(aboutArgs);
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

    /** Initialize settings, display tutorial, etc. */
    private void firstLaunchChecks() {
        // Creates UUID if one does not exist yet
        AppUtils.getUUID(this);

        // Initialize settings, if necessary
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // Check if this is the first time the app is being launched
        if (PrefUtils.isFirstLaunch(this)) {
            LOGI(TAG, "First launch");

            PrefUtils.markFirstLaunch(this);
            Analytics.queueEvent(this, Analytics.NEW_INSTALL, null);
        }

        // Currently this causes a crash
        // See T7 for more info
        //runTutorial();
    }

    private void runTutorial() {
        LOGV(TAG, "Current tutorial stage: " + PrefUtils.getTutorialStage(this));
        switch (PrefUtils.getTutorialStage(this)) {
            case 0: {
                showListPrefDialog(PrefUtils.KEY_PREF_HOME_CAMPUS,
                        R.string.pref_campus_title,
                        R.array.pref_campus_strings,
                        R.array.pref_campus_values);
                break;
            }

            case 1: {
                showListPrefDialog(PrefUtils.KEY_PREF_USER_TYPE,
                        R.string.pref_user_type_title,
                        R.array.pref_user_type_strings,
                        R.array.pref_user_type_values);
                break;
            }

            case 2: {
                // Determine whether to run nav drawer tutorial
                if (PrefUtils.hasDrawerBeenUsed(MainActivity.this)) {
                    mShowDrawerShowcase = false;
                    PrefUtils.advanceTutorialStage(MainActivity.this);
                } else {
                    mShowDrawerShowcase = true;
                    LOGI(TAG, "Drawer never opened before, show tutorial!");
                    showDrawerShowcase();
                }
                break;
            }
        }
    }

    /** Display a single-choice selection dialog for a list-based preference. */
    private void showListPrefDialog(@NonNull final String prefKey, int titleResId, int choicesResId, int valuesResId) {
        if (isFinishing() || getResources() == null) return;

        final String[] choicesArray = getResources().getStringArray(choicesResId);
        final String[] valsArray = getResources().getStringArray(valuesResId);

        AlertDialog.Builder prefDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setSingleChoiceItems(choicesArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        sharedPreferences.edit().putString(prefKey, valsArray[i]).apply();
                    }
                })
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog prefDialog = prefDialogBuilder.create();
        prefDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                PrefUtils.advanceTutorialStage(MainActivity.this);
                runTutorial();
            }
        });
        prefDialog.show();
    }

    /** Display the drawer showcase. */
    private void showDrawerShowcase() {
//        if (isFinishing()) return;
//
//        if (mShowDrawerShowcase && !mDisplayingTutorial) {
//            new ShowcaseView.Builder(this)
//                    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
//                    .setContentTitle(R.string.tutorial_welcome_title)
//                    .setContentText(R.string.tutorial_welcome_text)
//                    .setStyle(R.style.RutgersShowcaseTheme)
//                    .setShowcaseEventListener(new OnShowcaseEventListener() {
//                        @Override
//                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
//                            // ShowcaseView was told to hide.
//                        }
//
//                        @Override
//                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//                            // ShowcaseView has been fully hidden.
//                            mDisplayingTutorial = false;
//                        }
//
//                        @Override
//                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
//                            mDisplayingTutorial = true;
//                        }
//                    })
//                    .hideOnTouchOutside()
//                    .build();
//        }
    }

    /*
     * Nav drawer helpers
     */
    
    /**
     * Try to grab web hosted channels, add the native packaged channels on failure.
     */
    private void loadChannels() {
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Request.jsonArray("ordered_content.json", Request.CACHE_ONE_DAY)).done(new DoneCallback<JSONArray>() {

            @Override
            public void onDone(JSONArray channelsArray) {
                mChannelManager.loadChannelsFromJSONArray(channelsArray);
                addChannels(mChannelManager.getChannels());
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                mChannelManager.loadChannelsFromResource(getResources(), R.raw.channels);
                addChannels(mChannelManager.getChannels());
            }

        });
    }

    /**
     * Add channels to navigation drawer.
     * @param channels Channels to add to nav drawer
     */
    private void addChannels(List<Channel> channels) {
        mDrawerAdapter.addAll(channels);
    }
    /*
     * Fragment display methods
     */



    private boolean switchDrawerFragments(@NonNull Bundle args) {
        final String handleTag = args.getString(ComponentFactory.ARG_HANDLE_TAG);

        if (AppUtils.isOnTop(this, handleTag) && !handleTag.equals(WebDisplay.HANDLE)) {
            return false;
        }

        return switchFragments(args);
    }


    /**
     * Add current fragment to the backstack and switch to the new one defined by given arguments.
     * For calls from the nav drawer, this will attempt to pop all backstack history until the last
     * time the desired channel was launched.
     * @param args Argument bundle with at least 'component' argument set to describe which
     *             component to build. All other arguments will be passed to the new fragment.
     * @return True if the new fragment was successfully created, false if not.
     */
    public boolean switchFragments(@NonNull Bundle args) {
        final String handleTag = args.getString(ComponentFactory.ARG_HANDLE_TAG);
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


        // If this is a top level (nav drawer) press, find the last time this channel was launched
        // and pop backstack to it
//        if (isTopLevel && fm.findFragmentByTag(handleTag) != null) {
//            //fm.popBackStackImmediate(componentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            //hacky way to disable animations
//            FragmentUtils.fDisableAnimations = true;
//            fm.popBackStackImmediate(handleTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            FragmentUtils.fDisableAnimations = false;
//        }
        
        // Switch the main content fragment
        fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, fragment, handleTag)
                .addToBackStack(handleTag)
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
            LOGE(TAG, Log.getStackTraceString(e));
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
            LOGE(TAG, Log.getStackTraceString(e));
        }
        Analytics.queueEvent(this, Analytics.ERROR, extras);
    }

}
