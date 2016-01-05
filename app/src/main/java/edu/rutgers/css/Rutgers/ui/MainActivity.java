package edu.rutgers.css.Rutgers.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.interfaces.ChannelManagerProvider;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;
import edu.rutgers.css.Rutgers.link.fragments.LinkLoadFragment;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.model.DrawerAdapter;
import edu.rutgers.css.Rutgers.model.Motd;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.MainScreen;
import edu.rutgers.css.Rutgers.ui.fragments.MotdDialogFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Main activity. Handles navigation drawer, displayed fragments, and connection to location services.
 */

public class MainActivity extends GoogleApiProviderActivity implements
        ChannelManagerProvider, LoaderManager.LoaderCallbacks<MainActivityLoader.InitLoadHolder> {

    /** Log tag */
    private static final String TAG = "MainActivity";

    /* Member data */
    private ChannelManager mChannelManager;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerAdapter mDrawerAdapter;
    private boolean mShowedMotd;

    /* Constants */
    private static final int LOADER_ID = 1;
    public static final String SHOWED_MOTD = "showedMotd";

    /* View references */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;

    /* Callback for changed preferences */
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    /* Mediators */
    private FragmentMediator fragmentMediator;
    private TutorialMediator tutorialMediator;

    @Override
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // See if we've showed the Motd before
        if (savedInstanceState != null) {
            mShowedMotd = savedInstanceState.getBoolean(SHOWED_MOTD);
        }

        ApiRequest.enableCache(this);

        tutorialMediator = new TutorialMediator(this);

        LOGD(TAG, "UUID: " + AppUtils.getUUID(this));
        mChannelManager = new ChannelManager();

        firstLaunchChecks();

        // Enable drawer icon
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Set up navigation drawer
        mDrawerAdapter = new DrawerAdapter(this, R.layout.row_drawer_item, R.layout.row_divider, new ArrayList<Channel>());
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.actbar_new));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                tutorialMediator.markDrawerUsed();
                AppUtils.closeKeyboard(MainActivity.this);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerAdapter adapter = (DrawerAdapter) parent.getAdapter();
                if (adapter.positionIsSettings(position)) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    mDrawerLayout.closeDrawer(mDrawerListView);
                    return;
                } else if (adapter.positionIsAbout(position)) {
                    Bundle aboutArgs = AboutDisplay.createArgs();
                    fragmentMediator.switchFragments(aboutArgs);
                    mDrawerLayout.closeDrawer(mDrawerListView);
                    return;
                }

                Channel channel = (Channel) parent.getAdapter().getItem(position);
                Bundle channelArgs = channel.getBundle();
                String homeCampus = RutgersUtils.getHomeCampus(MainActivity.this);

                channelArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));

                mDrawerListView.invalidateViews();
                // Launch component
                fragmentMediator.switchFragments(channelArgs);

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

        fragmentMediator = new MainFragmentMediator(this);

        // Load the drawer content synchronously first. This will allow us to have something in the
        // drawer while we're loading. When the loader comes back it will erase this and fill
        // in new values
        JsonArray array = AppUtils.loadRawJSONArray(getResources(), R.raw.channels);
        if (array != null) {
            mChannelManager.loadChannelsFromJSONArray(array);
            mDrawerAdapter.addAll(mChannelManager.getChannels());
        }

        if (wantsLink()) {
            deepLink();
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, new MainScreen(), MainScreen.HANDLE)
                    .commit();
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOWED_MOTD, mShowedMotd);
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
    public void onBackPressed() {
        // If drawer is open, intercept back press to close drawer
        if (mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView);
            return;
        }

        if (!fragmentMediator.backPressWebView()) {
            LOGV(TAG, "Back button pressed. Leaving top component: " + AppUtils.topHandle(this));
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // See if ActionBarDrawerToggle will handle event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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

        tutorialMediator.runTutorial();
    }

    public FragmentMediator getFragmentMediator() {
        return fragmentMediator;
    }

    public void showDialogFragment(@NonNull DialogFragment dialogFragment, @NonNull String tag) {
        tutorialMediator.showDialogFragment(dialogFragment, tag);
    }

    @Override
    public Loader<MainActivityLoader.InitLoadHolder> onCreateLoader(int id, Bundle args) {
        return new MainActivityLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<MainActivityLoader.InitLoadHolder> loader, MainActivityLoader.InitLoadHolder holder) {
        // These will be non-null on success
        if (holder.getArray() == null || holder.getMotd() == null) {
            AppUtils.showFailedLoadToast(MainActivity.this);
            return;
        }

        final Motd motd = holder.getMotd();
        final JsonArray array = holder.getArray();
        if (!mShowedMotd && motd.getData() != null) {
            mShowedMotd = true;
            if (!motd.isWindow()) {
                // show a popup with the Motd
                MotdDialogFragment f = MotdDialogFragment.newInstance(motd.getTitle(), motd.getMotd());
                f.show(getSupportFragmentManager(), MotdDialogFragment.TAG);
            } else {
                // switch to a fullscreen Motd
                // we have to cheat to do this, but since the goal
                // is to brick the app it doesn't matter too much
                switchFragmentsOnLoadFinished(
                        TextDisplay.createArgs(motd.getTitle(), motd.getMotd()));
                if (!motd.hasCloseButton()) {
                    // Lock the user to the Motd (bricks the app on purpose)
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                return;
            }
        } else {
            LOGI(TAG, motd.getMotd());
        }

        // Load nav drawer items
        mChannelManager.clear();
        mChannelManager.loadChannelsFromJSONArray(array);

        mDrawerAdapter.clear();
        mDrawerAdapter.addAll(mChannelManager.getChannels());

        if (!wantsLink()) {
            mDrawerLayout.openDrawer(mDrawerListView);
        }
    }

    @Override
    public void onLoaderReset(Loader<MainActivityLoader.InitLoadHolder> loader) {
        mChannelManager.clear();
        mDrawerAdapter.clear();
    }

    /**
     * Determine if the current intent will cause deep linking
     * @return True if the intent has a uri
     */
    private boolean wantsLink() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        return action.equals(Intent.ACTION_VIEW) && data != null;
    }

    /**
     * Perform the deep linking. This breaks apart the input URI and
     * creates a LinkLoadFragment for parsing the URI and launching the
     * destination fragment.
     */
    private void deepLink() {
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final Uri data = intent.getData();

        if (action.equals(Intent.ACTION_VIEW) && data != null) {
            LOGI(TAG, "Intent received: action: " + action + " data: " + data.toString());

            final List<String> pathParts = new ArrayList<>();
            final List<String> encodedPathParts;
            final String handle;
            if (data.getScheme().equals("rutgers")) {
                // scheme "rutgers://<channel>/<args>"
                encodedPathParts = data.getPathSegments();
                handle = data.getHost();
            } else {
                // scheme "http://rumobile.rutgers.edu/link/<channel>/<args>"
                encodedPathParts = data.getPathSegments().subList(2, data.getPathSegments().size());
                handle = data.getPathSegments().get(1);
            }

            // Change arguments into normal strings
            for (final String part : encodedPathParts) {
                pathParts.add(Uri.decode(part));
            }

            // Launch the processing fragment
            final Channel channel = mChannelManager.getChannelByTag(handle);
            if (channel != null) {
                final Fragment taskFragment = new LinkLoadFragment();
                final Bundle taskArgs = LinkLoadFragment.createBundle(channel, pathParts);
                taskFragment.setArguments(taskArgs);
                getSupportFragmentManager().beginTransaction().add(taskFragment, LinkLoadFragment.TAG).commit();
            }
        }
    }

    /**
     * You're not supposed to switch fragments after a loader
     * but we're going to do it anyway cause we roll like that.
     *
     * Don't use this unless it's for a good reason
     * @param args Args for the fragment to switch to
     */
    private void switchFragmentsOnLoadFinished(final Bundle args) {
        final int WAT = 1;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WAT) {
                    fragmentMediator.switchFragments(args);
                }
            }
        };
        handler.sendEmptyMessage(WAT);
    }
}
