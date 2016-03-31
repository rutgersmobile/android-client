package edu.rutgers.css.Rutgers.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class that controls the Floating action button
 */
public final class ToggleFloatingActionButton {

    final Context context;
    final FloatingActionButton mainFab;
    final List<FloatingActionButton> fabs;

    private static final int PADDING_DP = 5;
    private static final int MAIN_DP = 56;
    private static final int FABS_DP = 40;
    private static final long ANIM_DURATION = 250;
    private static final float INIT_POS = 0;

    private final int mainSize;
    private final int fabSize;

    private boolean open = false;

    public ToggleFloatingActionButton(final @NonNull Context context, final @NonNull FloatingActionButton mainFab, final @NonNull ArrayList<FloatingActionButton> fabs) {
        this.context = context;
        this.mainFab = mainFab;
        this.fabs = fabs;

        mainSize = getMainHeight();
        fabSize = getFabHeight();

        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (open) {
                    closeFab();
                } else {
                    openFab();
                }
            }
        });
    }

    public int getMainHeight() {
        return dpToPx(MAIN_DP);
    }

    public int getFabHeight() {
        return dpToPx(FABS_DP);
    }

    int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public void addFab(final @NonNull FloatingActionButton fab) {
        fabs.add(fab);
    }

    public void addAll(final @NonNull Collection<FloatingActionButton> fabs) {
        for (final FloatingActionButton fab : fabs) {
            this.fabs.add(fab);
        }
    }

    public void showFab() {
        mainFab.setVisibility(View.VISIBLE);
        setFabsVisibility(View.VISIBLE);
    }

    public void hideFab() {
        mainFab.setVisibility(View.GONE);
        setFabsVisibility(View.GONE);
    }

    void setFabsVisibility(int visibility) {
        for (final FloatingActionButton fab : fabs) {
            fab.setVisibility(visibility);
        }
    }

    public void openFab() {
        setFabsVisibility(View.VISIBLE);
        final int padding = dpToPx(PADDING_DP);
        final int mainOffset = mainSize + padding;
        int fabsOffset = fabSize + padding;
        for (int i = 0; i < fabs.size(); i++) {
            final int position = i * fabsOffset + mainOffset;
            final FloatingActionButton fab = fabs.get(i);
            ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(fab, "translationY", - position);
            shareAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            shareAnimator.setDuration(ANIM_DURATION);
            shareAnimator.start();
        }

        ObjectAnimator rotateFab = ObjectAnimator.ofFloat(mainFab, "rotation", 45);
        rotateFab.setDuration(ANIM_DURATION);
        rotateFab.start();

        open = true;
    }

    public void closeFab() {
        for (final FloatingActionButton fab : fabs) {
            ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(fab, "translationY", INIT_POS);
            shareAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            shareAnimator.setDuration(ANIM_DURATION);
            shareAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            shareAnimator.start();
        }

        ObjectAnimator rotateFab = ObjectAnimator.ofFloat(mainFab, "rotation", 90);
        rotateFab.setDuration(ANIM_DURATION);
        rotateFab.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                mainFab.setRotation(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        rotateFab.start();

        open = false;
    }

    public boolean isOpen() {
        return open;
    }
}
