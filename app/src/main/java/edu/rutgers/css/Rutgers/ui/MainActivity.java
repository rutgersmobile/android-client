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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.interfaces.ChannelManagerProvider;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.model.DrawerAdapter;
import edu.rutgers.css.Rutgers.model.Motd;
import edu.rutgers.css.Rutgers.model.MotdAPI;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.MainScreen;
import edu.rutgers.css.Rutgers.ui.fragments.MotdDialogFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
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

    public static final String PREF_HANDLE_TAG = "handleTag";
    public static final String SHOWED_MOTD = "showedMotd";
    public static final String FRAGMENT_ARGS = "fragmentArgs";
    public static final String LAST_FRAGMENT_TAG = "lastFragmentTag";

    private boolean showedMotd = false;

    /* Member data */
    private ChannelManager mChannelManager;
    private ComponentFactory mComponentFactory;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerAdapter mDrawerAdapter;

    /** Flags whether the drawer tutorial _should_ be displayed. */
    private boolean mShowDrawerShowcase;

    /** Flags whether the drawer tutorial _is_ being displayed. */
    private boolean mDisplayingTutorial;

    /* View references */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private Toolbar mToolbar;

    /* Callback for changed preferences */
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private final FragmentManager fm = getSupportFragmentManager();

    private String lastFragmentTag;

    @Override
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            showedMotd = savedInstanceState.getBoolean(SHOWED_MOTD);
            lastFragmentTag = savedInstanceState.getString(LAST_FRAGMENT_TAG);
        } else {
            lastFragmentTag = getPreferences(Context.MODE_PRIVATE).getString(PREF_HANDLE_TAG, null);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (!showedMotd) {
            mToolbar.setVisibility(View.GONE);
        }
        setSupportActionBar(mToolbar);

        LOGD(TAG, "UUID: " + AppUtils.getUUID(this));
        mComponentFactory = new ComponentFactory();
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
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.actbar_new));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
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
                DrawerAdapter adapter = (DrawerAdapter) parent.getAdapter();
                if (adapter.positionIsSettings(position)) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    mDrawerLayout.closeDrawer(mDrawerListView);
                    return;
                } else if (adapter.positionIsAbout(position)) {
                    Bundle aboutArgs = AboutDisplay.createArgs();
                    aboutArgs.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);
                    switchDrawerFragments(aboutArgs);
                    mDrawerLayout.closeDrawer(mDrawerListView);
                    return;
                }

                Channel channel = (Channel) parent.getAdapter().getItem(position);
                Bundle channelArgs = channel.getBundle();
                String homeCampus = RutgersUtils.getHomeCampus(MainActivity.this);

                channelArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
                channelArgs.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);

                mDrawerListView.setItemChecked(position, true);
                LOGI(TAG, "Currently checked item position: " + mDrawerListView.getCheckedItemPosition());

                mDrawerListView.invalidateViews();
                // Launch component
                switchDrawerFragments(channelArgs);

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

        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                String fragmentTag = null;
                if (fm.getBackStackEntryCount() > 0) {
                    FragmentManager.BackStackEntry backStackEntry;
                    backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
                    fragmentTag = backStackEntry.getName();
                }
                if (fragmentTag != null && mChannelManager.getChannelByTag(fragmentTag) != null) {
                    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString(PREF_HANDLE_TAG, fragmentTag).apply();
                    if (lastFragmentTag == null && fm.getBackStackEntryCount() == 1) {
                        lastFragmentTag = fragmentTag;
                    }
                    LOGI(TAG, "Stored channel tag: " + fragmentTag);
                }
                highlightCorrectDrawerItem();
            }
        });

        loadCorrectFragment(savedInstanceState);
    }

    private void loadCorrectFragment(Bundle savedInstanceState) {
        Map<String, Channel> channelsMap = RutgersApplication.getChannelsMap();

        if (channelsMap == null) {
            MotdAPI.getMotd().fail(new FailCallback<AjaxStatus>() {
                @Override
                public void onFail(AjaxStatus result) {
                    LOGE(TAG, "Couldn't get MOTD");
                }
            }).always(new AlwaysCallback<Motd, AjaxStatus>() {
                @Override
                public void onAlways(Promise.State state, final Motd motd, AjaxStatus rejected) {
                    if (!showedMotd) {
                        showedMotd = true;
                        if (motd != null && !motd.isWindow()) {
                            MotdDialogFragment f = MotdDialogFragment.newInstance(motd.getTitle(), motd.getMotd());
                            f.show(fm, MotdDialogFragment.TAG);
                        } else if (motd != null) {
                            switchFragments(TextDisplay.createArgs(motd.getTitle(), motd.getMotd()));
                            if (!motd.hasCloseButton()) {
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                }
                                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            }
                            return;
                        }
                    }
                    mToolbar.setVisibility(View.VISIBLE);
                    // Load nav drawer items
                    loadChannels().always(new AlwaysCallback<JSONArray, AjaxStatus>() {
                        @Override
                        public void onAlways(Promise.State state, JSONArray resolved, AjaxStatus rejected) {
                            restore(true);
                        }
                    });
                }
            });

            if (fm.getBackStackEntryCount() == 0) {
                fm.beginTransaction()
                        .replace(R.id.main_content_frame, new MainScreen(), MainScreen.HANDLE)
                        .commit();
            }
        } else {
            mChannelManager.setChannelsMap(channelsMap);
            mDrawerAdapter.addAll(mChannelManager.getChannels());
            mToolbar.setVisibility(View.VISIBLE);
            if (savedInstanceState == null) {
                restore(false);
            }
        }
    }

    private boolean restore(boolean anim) {
        if (lastFragmentTag != null && !lastFragmentTag.equals(MainScreen.HANDLE)) {
            Channel channel = mChannelManager.getChannelByTag(lastFragmentTag);
            Bundle initialFragmentBundle;
            if (channel == null) {
                //hack to go back to 'about' page if the last viewed fragment was about or had an invalid tag.
                LOGE(TAG, "Invalid Channel saved in preferences.handleTag: " + lastFragmentTag);
                SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
                pref.edit().remove(PREF_HANDLE_TAG);
                pref.edit().apply();
                initialFragmentBundle = AboutDisplay.createArgs();
                initialFragmentBundle.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);
                mDrawerListView.setItemChecked(mDrawerAdapter.getAboutPosition(), true);
            } else {
                initialFragmentBundle = channel.getBundle();
                int position = mDrawerAdapter.getPosition(channel);
                mDrawerListView.setItemChecked(position, true);
            }
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_ANIM_BOTTOM, true);
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_ANIM, anim);
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_BACKSTACK, false);
            return switchDrawerFragments(initialFragmentBundle);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOWED_MOTD, showedMotd);
        outState.putString(LAST_FRAGMENT_TAG, lastFragmentTag);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        highlightCorrectDrawerItem();
        super.onResume();

//        showDrawerShowcase();
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

        if (lastFragmentTag != null) {
            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(PREF_HANDLE_TAG, lastFragmentTag).apply();
        }

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
        if (AppUtils.isComponentOnTop(this, mChannelManager, WebDisplay.HANDLE)) {
            // Get the fragment on top of the back stack
            Fragment webView = fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
            if (webView != null && webView.isVisible()) {
                if (((WebDisplay) webView).backPress()) {
                    LOGD(TAG, "Triggered WebView back button");
                    return;
                }
            }
        }

        LOGV(TAG, "Back button pressed. Leaving top component: " + AppUtils.topHandle(this));
        super.onBackPressed();
    }

    /**
     * Change which menu item is highlighted to the correct currently displayed fragment. used for
     * changing the backstack when the back button is pressed and when changing activities from
     * settings to main
     * */
    private void highlightCorrectDrawerItem() {
        if(fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
            String fragmentTag = backStackEntry.getName();
            if (fragmentTag == null) {
                return;
            }
            mDrawerListView.setItemChecked(mDrawerListView.getCheckedItemPosition(), false);
            if (fragmentTag.equals(AboutDisplay.HANDLE)) {
                mDrawerListView.setItemChecked(mDrawerAdapter.getAboutPosition(), true);
                return;
            }
            Channel channel = mChannelManager.getChannelByTag(fragmentTag);
            mDrawerListView.setItemChecked(mDrawerAdapter.getPosition(channel), true);
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

        runTutorial();
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
                    // Currently this causes a crash
                    // See T7 for more info
//                    showDrawerShowcase();
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

    // TODO Get this to work
    /** Display the drawer showcase. */
    private void showDrawerShowcase() {
        if (isFinishing()) return;

        if (mShowDrawerShowcase && !mDisplayingTutorial) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
                    .setContentTitle(R.string.tutorial_welcome_title)
                    .setContentText(R.string.tutorial_welcome_text)
//                    .setStyle(R.style.RutgersShowcaseTheme)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            // ShowcaseView was told to hide.
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            // ShowcaseView has been fully hidden.
                            mDisplayingTutorial = false;
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            mDisplayingTutorial = true;
                        }
                    })
                    .hideOnTouchOutside()
                    .build();
        }
    }

    /*
     * Nav drawer helpers
     */

    /**
     * Try to grab web hosted channels, add the native packaged channels on failure. Load the last channel onAlways.
     */
    private Promise<JSONArray, AjaxStatus, Double> loadChannels() {
        AndroidDeferredManager dm = new AndroidDeferredManager();
        return dm.when(Request.jsonArray("ordered_content.json", Request.CACHE_ONE_DAY)).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray channelsArray) {
                mChannelManager.loadChannelsFromJSONArray(channelsArray);
                RutgersApplication.setChannelsMap(mChannelManager.getChannelsMap());
                mDrawerAdapter.addAll(mChannelManager.getChannels());
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                mChannelManager.loadChannelsFromResource(getResources(), R.raw.channels);
                RutgersApplication.setChannelsMap(mChannelManager.getChannelsMap());
                mDrawerAdapter.addAll(mChannelManager.getChannels());
            }
        });
    }

    /*
     * Fragment display methods
     */

    private boolean switchDrawerFragments(@NonNull Bundle args) {
        final String handleTag = args.getString(ComponentFactory.ARG_HANDLE_TAG);

        return !AppUtils.isOnTop(this, handleTag) && switchFragments(args);
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
        final boolean animBottom = args.getBoolean(ComponentFactory.ARG_ANIM_BOTTOM);
        final boolean backstack = args.getBoolean(ComponentFactory.ARG_BACKSTACK, true);
        final boolean anim = args.getBoolean(ComponentFactory.ARG_ANIM, true);

        // Attempt to create the fragment
        final Fragment fragment = mComponentFactory.createFragment(args);
        if (fragment == null) {
            Analytics.sendChannelErrorEvent(this, args); // Channel launch failure analytics event
            return false;
        } else {
            Analytics.sendChannelEvent(this, args); // Channel launch analytics event
        }

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtils.closeKeyboard(this);

        // Switch the main content fragment
        FragmentTransaction ft = fm.beginTransaction();
        if (anim && animBottom) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_bottom);
        } else if (anim) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        }
        ft.replace(R.id.main_content_frame, fragment, handleTag);
        if (backstack) {
            if (mChannelManager.getChannelByTag(handleTag) != null) {
                ft.addToBackStack(handleTag);
            } else {
                ft.addToBackStack(null);
            }
        }
        ft.commit();

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
}
