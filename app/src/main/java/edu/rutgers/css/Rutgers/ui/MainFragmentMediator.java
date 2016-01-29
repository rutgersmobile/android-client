package edu.rutgers.css.Rutgers.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;
import edu.rutgers.css.Rutgers.link.LinkLoadArgs;
import edu.rutgers.css.Rutgers.link.LinkLoadTask;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

/**
 * Control where we're putting fragments
 */
public class MainFragmentMediator implements FragmentMediator {

    private static final String TAG = "MainFragmentMediator";

    private final FragmentManager fm;
    private final MainActivity activity;
    private final ComponentFactory componentFactory;

    public MainFragmentMediator(final MainActivity activity) {
        this.fm = activity.getSupportFragmentManager();
        this.activity = activity;
        this.componentFactory = new ComponentFactory();
    }

    /**
     * Add current fragment to the backstack and switch to the new one defined by given arguments.
     * For calls from the nav drawer, this will attempt to pop all backstack history until the last
     * time the desired channel was launched.
     * @param args Argument bundle with at least 'component' argument set to describe which
     *             component to build. All other arguments will be passed to the new fragment.
     * @return True if the new fragment was successfully created, false if not.
     */
    @Override
    public boolean switchFragments(@NonNull final Bundle args) {
        if (activity.isFinishing() || !RutgersApplication.isApplicationVisible()) return false;

        final String componentTag = args.getString(ComponentFactory.ARG_COMPONENT_TAG);
        final boolean backStack = args.getBoolean(ComponentFactory.ARG_BACKSTACK_TAG, true);

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
        FragmentTransaction ft = fm.beginTransaction()
                // There's a bug in the support library that will
                // cause crashes with custom animations
                // .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                //     R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, fragment, componentTag);
        if (backStack) {
            ft = ft.addToBackStack(componentTag);
        }
        ft.commit();

        return true;
    }

    /**
     * Perform the deep linking. This breaks apart the input URI and
     * creates a LinkLoadTask for parsing the URI and launching the
     * destination fragment.
     */
    @Override
    public void deepLink(@NonNull final Uri uri, final boolean backstack) {
        LOGI(TAG, "Linking : " + uri.toString());

        final String homeCampus = RutgersUtils.getHomeCampus(activity);
        final List<String> pathParts = new ArrayList<>();
        final List<String> encodedPathParts;
        final String handle;
        if (uri.getScheme().equals("rutgers")) {
            // scheme "rutgers://<channel>/<args>"
            encodedPathParts = uri.getPathSegments();
            handle = uri.getHost();
        } else {
            // scheme "http://rumobile.rutgers.edu/link/<channel>/<args>"
            encodedPathParts = uri.getPathSegments().subList(2, uri.getPathSegments().size());
            handle = uri.getPathSegments().get(1);
        }

        // Change arguments into normal strings
        for (final String part : encodedPathParts) {
            pathParts.add(Uri.decode(part));
        }

        final Channel channel = activity.getChannelManager().getChannelByTag(handle);
        if (channel != null) {
            final String channelTag = channel.getView();

            // Launch channel immediately if that's all we have
            if (pathParts.size() == 0) {
                LOGI(TAG, "Linking to channel: " + channelTag);
                Bundle channelArgs = channel.getBundle();

                channelArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
                switchFragments(channelArgs);
                return;
            }

            // We don't have a reference to the enclosing activity in the task
            // so we have to get all this information here
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
            final String prefLevel = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
            final String prefCampus = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
            final String prefSemester = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null);

            new LinkLoadTask(homeCampus, prefCampus, prefLevel, prefSemester, backstack)
                    .execute(new LinkLoadArgs(channel, pathParts));
        }
    }

    @Override
    public boolean backPressWebView() {
        // If web display is active, send back button presses to it for navigating browser history
        if (AppUtils.isOnTop(activity, WebDisplay.HANDLE)) {
            // Get the fragment on top of the back stack
            Fragment webView = fm.findFragmentByTag(WebDisplay.HANDLE);
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
