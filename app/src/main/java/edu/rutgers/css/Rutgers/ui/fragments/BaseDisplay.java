package edu.rutgers.css.Rutgers.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.animation.Animation;

import edu.rutgers.css.Rutgers.utils.FragmentUtils;

/**
 * Base class to remove animations when switching fragments
 */
public class BaseDisplay extends Fragment {
    //hack to prevent animations from occurring when popping from backstack
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.fDisableAnimations) {
            Animation a = new Animation() {};
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
