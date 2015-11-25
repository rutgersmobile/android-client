package edu.rutgers.css.Rutgers.interfaces;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Mediator interface for managing fragments in an activity
 */
public interface FragmentMediator {
    boolean switchFragments(@NonNull Bundle args);
    boolean backPressWebView();
}
