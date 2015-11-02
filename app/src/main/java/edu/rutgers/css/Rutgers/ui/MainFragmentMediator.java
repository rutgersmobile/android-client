package edu.rutgers.css.Rutgers.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;

import java.util.Map;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;
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

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

/**
 * Control where we're putting fragments
 */
public class MainFragmentMediator implements FragmentMediator {

    private static final String TAG = "MainFragmentMediator";

    private boolean showedMotd = false;
    private String lastFragmentTag;

    public String getLastFragmentTag() {
        return lastFragmentTag;
    }

    public static final String PREF_HANDLE_TAG = "handleTag";
    public static final String SHOWED_MOTD = "showedMotd";
    public static final String LAST_FRAGMENT_TAG = "lastFragmentTag";


    private final FragmentManager fm;
    private final Toolbar toolbar;
    private final AppCompatActivity activity;
    private final DrawerLayout drawerLayout;
    private final ChannelManager channelManager;
    private final DrawerAdapter drawerAdapter;
    private final ListView drawerListView;
    private final ComponentFactory componentFactory;
    private final Bundle savedInstanceState;

    public MainFragmentMediator(final AppCompatActivity activity, final Toolbar toolbar, final DrawerLayout drawerLayout,
                                final DrawerAdapter drawerAdapter, final ChannelManager channelManager,
                                final ListView drawerListView, final Bundle savedInstanceState) {
        this.fm = activity.getSupportFragmentManager();
        this.toolbar = toolbar;
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.channelManager = channelManager;
        this.drawerAdapter = drawerAdapter;
        this.drawerListView = drawerListView;
        this.componentFactory = new ComponentFactory();
        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            showedMotd = savedInstanceState.getBoolean(SHOWED_MOTD);
            lastFragmentTag = savedInstanceState.getString(LAST_FRAGMENT_TAG);
        } else {
            lastFragmentTag = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_HANDLE_TAG, null);
        }

        if (!showedMotd) {
            toolbar.setVisibility(View.GONE);
        }

        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                String fragmentTag = null;
                if (fm.getBackStackEntryCount() > 0) {
                    FragmentManager.BackStackEntry backStackEntry;
                    backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
                    fragmentTag = backStackEntry.getName();
                }
                if (fragmentTag != null && channelManager.getChannelByTag(fragmentTag) != null) {
                    SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString(PREF_HANDLE_TAG, fragmentTag).apply();
                    if (lastFragmentTag == null && fm.getBackStackEntryCount() == 1) {
                        lastFragmentTag = fragmentTag;
                    }
                    LOGI(TAG, "Stored channel tag: " + fragmentTag);
                }
                highlightCorrectDrawerItem();
            }
        });
    }

    public void loadCorrectFragment() {
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
                    if (!showedMotd && motd != null && motd.getData() != null) {
                        showedMotd = true;
                        if (!motd.isWindow()) {
                            MotdDialogFragment f = MotdDialogFragment.newInstance(motd.getTitle(), motd.getMotd());
                            f.show(fm, MotdDialogFragment.TAG);
                        } else {
                            switchFragments(
                                    TextDisplay.createArgs(motd.getTitle(), motd.getMotd()));
                            if (!motd.hasCloseButton()) {
                                if (activity.getSupportActionBar() != null) {
                                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                }
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            }
                            return;
                        }
                    } else if (motd != null) {
                        LOGI(TAG, motd.getMotd());
                    }
                    toolbar.setVisibility(View.VISIBLE);
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
            channelManager.setChannelsMap(channelsMap);
            drawerAdapter.addAll(channelManager.getChannels());
            toolbar.setVisibility(View.VISIBLE);
            if (savedInstanceState == null) {
                restore(false);
            }
        }
    }

    private boolean restore(boolean anim) {
        if (lastFragmentTag != null && !lastFragmentTag.equals(MainScreen.HANDLE)) {
            Channel channel = channelManager.getChannelByTag(lastFragmentTag);
            Bundle initialFragmentBundle;
            if (channel == null) {
                //hack to go back to 'about' page if the last viewed fragment was about or had an invalid tag.
                LOGE(TAG, "Invalid Channel saved in preferences.handleTag: " + lastFragmentTag);
                SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
                pref.edit().remove(PREF_HANDLE_TAG);
                pref.edit().apply();
                initialFragmentBundle = AboutDisplay.createArgs();
                initialFragmentBundle.putBoolean(ComponentFactory.ARG_TOP_LEVEL, true);
                drawerListView.setItemChecked(drawerAdapter.getAboutPosition(), true);
            } else {
                initialFragmentBundle = channel.getBundle();
                int position = drawerAdapter.getPosition(channel);
                drawerListView.setItemChecked(position, true);
            }
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_ANIM_BOTTOM, true);
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_ANIM, anim);
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_BACKSTACK, false);
            initialFragmentBundle.putBoolean(ComponentFactory.ARG_CREATE, true);
            return switchDrawerFragments(initialFragmentBundle);
        }
        return false;
    }

    public boolean switchDrawerFragments(@NonNull Bundle args) {
        final String handleTag = args.getString(ComponentFactory.ARG_HANDLE_TAG);

        return !AppUtils.isOnTop(activity, handleTag) && switchFragments(args);
    }

    /**
     * Try to grab web hosted channels, add the native packaged channels on failure. Load the last channel onAlways.
     */
    private Promise<JSONArray, AjaxStatus, Double> loadChannels() {
        AndroidDeferredManager dm = new AndroidDeferredManager();
        return dm.when(ApiRequest.apiArray("ordered_content.json", ApiRequest.CACHE_ONE_DAY)).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray channelsArray) {
                channelManager.loadChannelsFromJSONArray(channelsArray);
                RutgersApplication.setChannelsMap(channelManager.getChannelsMap());
                drawerAdapter.addAll(channelManager.getChannels());
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                channelManager.loadChannelsFromResource(activity.getResources(), R.raw.channels);
                RutgersApplication.setChannelsMap(channelManager.getChannelsMap());
                drawerAdapter.addAll(channelManager.getChannels());
            }
        });
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
        if (activity.isFinishing() || (!RutgersApplication.isApplicationVisible() && !args.getBoolean(ComponentFactory.ARG_CREATE))) return false;

        String handleTag = args.getString(ComponentFactory.ARG_HANDLE_TAG);
        if (handleTag == null) {
            handleTag = args.getString(ComponentFactory.ARG_COMPONENT_TAG);
        }
        final boolean animBottom = args.getBoolean(ComponentFactory.ARG_ANIM_BOTTOM);
        final boolean backstack = args.getBoolean(ComponentFactory.ARG_BACKSTACK, true);
        final boolean anim = args.getBoolean(ComponentFactory.ARG_ANIM, true);

        // Attempt to create the fragment
        final Fragment fragment = componentFactory.createFragment(args);
        if (fragment == null) {
            Analytics.sendChannelErrorEvent(activity, args); // Channel launch failure analytics event
            return false;
        } else {
            Analytics.sendChannelEvent(activity, args); // Channel launch analytics event
        }

        // Close soft keyboard, it's usually annoying when it stays open after changing screens
        AppUtils.closeKeyboard(activity);
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
            ft.addToBackStack(handleTag);
        }
        ft.commit();

        return true;
    }

    /**
     * Change which menu item is highlighted to the correct currently displayed fragment. used for
     * changing the backstack when the back button is pressed and when changing activities from
     * settings to main
     * */
    public void highlightCorrectDrawerItem() {
        if(fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
            String fragmentTag = backStackEntry.getName();
            if (fragmentTag == null) {
                return;
            }
            Channel channel = channelManager.getChannelByTag(fragmentTag);
            if (channel == null) {
                return;
            }
            drawerListView.setItemChecked(drawerListView.getCheckedItemPosition(), false);
            if (fragmentTag.equals(AboutDisplay.HANDLE)) {
                drawerListView.setItemChecked(drawerAdapter.getAboutPosition(), true);
                return;
            }
            drawerListView.setItemChecked(drawerAdapter.getPosition(channel), true);
        }
    }

    public void saveState(Bundle outState) {
        outState.putBoolean(SHOWED_MOTD, showedMotd);
        outState.putString(LAST_FRAGMENT_TAG, lastFragmentTag);
    }

    public void saveFragment() {
        if (lastFragmentTag != null) {
            SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(PREF_HANDLE_TAG, lastFragmentTag).apply();
        }
    }

    public boolean isFirstVisibleFragment(@NonNull String handle) {
        return fm.getBackStackEntryCount() == 0 && handle.equalsIgnoreCase(lastFragmentTag);
    }

    public boolean backPressWebView() {
        // If web display is active, send back button presses to it for navigating browser history
        if (AppUtils.isComponentOnTop(activity, channelManager, WebDisplay.HANDLE, lastFragmentTag)) {
            // Get the fragment on top of the back stack
            Fragment webView;
            if (fm.getBackStackEntryCount() > 0) {
                webView = fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
            } else {
                webView = fm.findFragmentByTag(lastFragmentTag);
            }
            if (webView != null && webView.isVisible()) {
                if (((WebDisplay) webView).backPress()) {
                    LOGD(TAG, "Triggered WebView back button");
                    return true;
                }
            }
        }
        return false;
    }
}
