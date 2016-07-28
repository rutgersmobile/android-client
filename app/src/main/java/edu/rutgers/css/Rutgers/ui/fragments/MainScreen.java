package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.ui.MainActivity;

public class MainScreen extends BaseDisplay {

    private static final String TAG = "MainScreen";
    public static final String HANDLE = "mainfrag";
    private static final String ARG_DRAWER_TAG = "openDrawer";
    private boolean openDrawer;

    public static Bundle createArgs(boolean openDrawer) {
        final Bundle args = new Bundle();
        args.putBoolean(ARG_DRAWER_TAG, openDrawer);
        return args;
    }

    public MainScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        openDrawer = args.getBoolean(ARG_DRAWER_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_main_screen, container, false);
        getActivity().setTitle(R.string.app_name);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        if (openDrawer) {
            ((MainActivity) getActivity()).openDrawer();
        } else {
            openDrawer = true;
        }

        return v;
    }

}
