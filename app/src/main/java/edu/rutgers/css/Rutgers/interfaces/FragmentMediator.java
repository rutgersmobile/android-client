package edu.rutgers.css.Rutgers.interfaces;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Mediator interface for managing fragments in an activity
 */
public interface FragmentMediator {
    void loadCorrectFragment();
    boolean switchFragments(@NonNull Bundle args);
    void saveState(Bundle outState);
    void saveFragment();

    boolean isFirstVisibleFragment(@NonNull String handle);
    void backPressWebView();

    boolean switchDrawerFragments(@NonNull Bundle args);
    void highlightCorrectDrawerItem();
}
