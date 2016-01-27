package edu.rutgers.css.Rutgers.ui.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Linkable;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends BaseDisplay implements Linkable {

    private ProgressBar mProgressCircle;
    private FloatingActionButton fab;
    private FloatingActionButton shareFab;
    private FloatingActionButton bookmarkFab;
    private boolean open = false;

    private static final long ANIM_DURATION = 250;
    private static final float INIT_POS = 0;
    private static final float SHARE_POS = -200;
    private static final float BOOKMARK_POS = -325;

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        final View v = inflater.inflate(resource, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        shareFab = (FloatingActionButton) v.findViewById(R.id.mini_share_fab);
        bookmarkFab = (FloatingActionButton) v.findViewById(R.id.mini_bookmark_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        closeFab();
                    } else {
                        openFab();
                    }
                }
            });

            shareFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        startActivity(Intent.createChooser(getShareIntent(), "Share via"));
                        closeFab();
                    }
                }
            });

            bookmarkFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        PrefUtils.addBookmark(getContext(), getLink());
                        Toast.makeText(getContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                        closeFab();
                    }
                }
            });
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
        fab = null;
        shareFab = null;
        bookmarkFab = null;
    }

    final protected void showProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    final protected void hideProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

    final protected void switchFragments(Bundle args) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).getFragmentMediator().switchFragments(args);
        }
    }

    final protected void showDialogFragment(DialogFragment dialogFragment, String tag) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).showDialogFragment(dialogFragment, tag);
        }
    }

    final protected void showFab() {
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
            shareFab.setVisibility(View.VISIBLE);
            bookmarkFab.setVisibility(View.VISIBLE);
        }
    }

    final protected void hideFab() {
        if (fab != null) {
            fab.setVisibility(View.GONE);
            shareFab.setVisibility(View.GONE);
            bookmarkFab.setVisibility(View.GONE);
        }
    }

    final protected void openFab() {
        ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(shareFab, "translationY", SHARE_POS);
        shareAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shareAnimator.setDuration(ANIM_DURATION);
        shareAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                shareFab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) { }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        shareAnimator.start();

        ObjectAnimator bookmarkAnimator = ObjectAnimator.ofFloat(bookmarkFab, "translationY", BOOKMARK_POS);
        bookmarkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        bookmarkAnimator.setDuration(ANIM_DURATION);
        bookmarkAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                bookmarkFab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        bookmarkAnimator.start();

        ObjectAnimator rotateFab = ObjectAnimator.ofFloat(fab, "rotation", 45);
        rotateFab.setDuration(ANIM_DURATION);
        rotateFab.start();

        open = true;
    }

    final protected void closeFab() {
        ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(shareFab, "translationY", INIT_POS);
        shareAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shareAnimator.setDuration(ANIM_DURATION);
        shareAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                shareFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        shareAnimator.start();

        final ObjectAnimator bookmarkAnimator = ObjectAnimator.ofFloat(bookmarkFab, "translationY", INIT_POS);
        bookmarkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        bookmarkAnimator.setDuration(ANIM_DURATION);
        bookmarkAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bookmarkFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        bookmarkAnimator.start();

        ObjectAnimator rotateFab = ObjectAnimator.ofFloat(fab, "rotation", 90);
        rotateFab.setDuration(ANIM_DURATION);
        rotateFab.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                fab.setRotation(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        rotateFab.start();

        open = false;
    }

    protected Intent getShareIntent() {
        Uri uri = getLink().getUri(Config.SCHEMA);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        return intent;
    }

    public void setShareIntent() {
        final Intent intent = getShareIntent();
        final ShareActionProvider shareActionProvider = getShareActionProvider();
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(intent);
        }
    }

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
