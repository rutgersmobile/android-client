package edu.rutgers.css.Rutgers.ui.fragments;

/**
 * Popup that shows Motd information
 */
public class LinkErrorDialogFragment extends InfoDialogFragment {
    public static final String TAG = "LinkErrorDialogFragment";

    public static final LinkErrorDialogFragment defaultError = (LinkErrorDialogFragment) newInstance("Link Error", "Invalid link");
}
