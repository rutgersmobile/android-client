package edu.rutgers.css.Rutgers.interfaces;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Mediator interface for managing fragments in an activity
 */
public interface FragmentMediator {
    boolean switchFragments(@NonNull final Bundle args);
    void deepLink(@NonNull final Uri uri, final boolean backstack);
    boolean backPressWebView();
}
