package edu.rutgers.css.Rutgers.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Controls the tutorial
 */
public class TutorialMediator {

    private final AppCompatActivity activity;
    private final FragmentManager fm;

    private final String TAG = "TutorialMediator";

    /** Flags whether the drawer tutorial _should_ be displayed. */
    private boolean showDrawerShowcase;

    /** Flags whether the drawer tutorial _is_ being displayed. */
    private boolean displayingTutorial;

    public TutorialMediator(AppCompatActivity activity) {
        this.activity = activity;
        this.fm = activity.getSupportFragmentManager();
    }

    public void runTutorial() {
        LOGV(TAG, "Current tutorial stage: " + PrefUtils.getTutorialStage(activity));
        switch (PrefUtils.getTutorialStage(activity)) {
            case 0: {
                showListPrefDialog(PrefUtils.KEY_PREF_HOME_CAMPUS,
                        R.string.pref_campus_title,
                        R.array.pref_campus_strings,
                        R.array.pref_campus_values);
                break;
            }

            case 1: {
                showListPrefDialog(PrefUtils.KEY_PREF_USER_TYPE,
                        R.string.pref_user_type_title,
                        R.array.pref_user_type_strings,
                        R.array.pref_user_type_values);
                break;
            }

            case 2: {
                // Determine whether to run nav drawer tutorial
                /*
                if (PrefUtils.hasDrawerBeenUsed(activity)) {
                    showDrawerShowcase = false;
                    PrefUtils.advanceTutorialStage(activity);
                } else {
                    showDrawerShowcase = true;
                    LOGI(TAG, "Drawer never opened before, show tutorial!");
                    // Show drawer tutorial here
                }
                */
                break;
            }
        }
    }

    /** Display a single-choice selection dialog for a list-based preference. */
    private void showListPrefDialog(@NonNull final String prefKey, int titleResId, int choicesResId, int valuesResId) {
        if (activity.isFinishing() || activity.getResources() == null) return;

        final String[] choicesArray = activity.getResources().getStringArray(choicesResId);
        final String[] valsArray = activity.getResources().getStringArray(valuesResId);

        AlertDialog.Builder prefDialogBuilder = new AlertDialog.Builder(activity)
                .setTitle(titleResId)
                .setSingleChoiceItems(choicesArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                        sharedPreferences.edit().putString(prefKey, valsArray[i]).apply();
                    }
                })
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog prefDialog = prefDialogBuilder.create();
        prefDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                PrefUtils.advanceTutorialStage(activity);
                runTutorial();
            }
        });
        prefDialog.show();
    }

    /**
     * Show a dialog fragment and add it to backstack
     * @param dialogFragment Dialog fragment to display
     * @param tag Tag for fragment transaction backstack
     */
    public void showDialogFragment(@NonNull DialogFragment dialogFragment, @NonNull String tag) {
        final FragmentTransaction ft = fm.beginTransaction();
        final Fragment prev = fm.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(tag);
        dialogFragment.show(ft, tag);
    }

    public void markDrawerUsed() {
        if (showDrawerShowcase) {
            PrefUtils.markDrawerUsed(activity);
            showDrawerShowcase = false;
            LOGI(TAG, "Drawer opened for first time.");
        }
    }
}
