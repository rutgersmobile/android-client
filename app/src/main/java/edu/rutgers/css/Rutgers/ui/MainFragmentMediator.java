package edu.rutgers.css.Rutgers.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.Analytics;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Control where we're putting fragments
 */
public class MainFragmentMediator implements FragmentMediator {

    private static final String TAG = "MainFragmentMediator";

    private final FragmentManager fm;
    private final AppCompatActivity activity;
    private final ComponentFactory componentFactory;

    public MainFragmentMediator(final AppCompatActivity activity) {
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
    public boolean switchFragments(@NonNull Bundle args) {
        if (activity.isFinishing() || !RutgersApplication.isApplicationVisible()) return false;

        final String componentTag = args.getString(ComponentFactory.ARG_COMPONENT_TAG);

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
        fm.beginTransaction()
                // There's a bug in the support library that will
                // cause crashes with custom animations
                // .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                //     R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, fragment, componentTag)
                .addToBackStack(componentTag)
                .commit();

        return true;
    }

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
