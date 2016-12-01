package edu.rutgers.css.Rutgers.interfaces;

import android.content.Intent;

/**
 * Used to pass Activity results to other parts of the application
 */
public interface OnActivityResultCallback {
    void onActivityResult(int requestCode, int resultCode, Intent intent);
}
