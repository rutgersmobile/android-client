package edu.rutgers.css.Rutgers.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Actions to perform in Application when Activities enter certain states
 */
public class MainActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private int resumed;
    private int paused;
    private int started;
    private int stopped;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) { }

    @Override
    public void onActivityStarted(Activity activity) {
        started++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumed++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        stopped++;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }

    public boolean isApplicationVisible() {
        return started > stopped;
    }

    public boolean isApplicationInForeground() {
        return resumed > paused;
    }
}
