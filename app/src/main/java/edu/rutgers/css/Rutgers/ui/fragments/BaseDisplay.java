package edu.rutgers.css.Rutgers.ui.fragments;

import android.view.animation.Animation;

import com.trello.rxlifecycle.components.support.RxFragment;

import edu.rutgers.css.Rutgers.utils.FragmentUtils;

/**
 * Base class to remove animations when switching fragments
 */
public class BaseDisplay extends RxFragment {
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
