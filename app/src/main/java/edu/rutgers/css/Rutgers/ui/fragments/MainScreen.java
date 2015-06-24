package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.R;

public class MainScreen extends BaseDisplay {

    private static final String TAG = "MainScreen";
    public static final String HANDLE = "mainfrag";

    public MainScreen() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_main_screen, container, false);
        getActivity().setTitle(R.string.app_name);
        return v;
    }

}
