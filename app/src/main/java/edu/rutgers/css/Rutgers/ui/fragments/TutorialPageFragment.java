package edu.rutgers.css.Rutgers.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialPageFragment extends Fragment {

    public TutorialPageFragment() {
        // Required empty public constructor
    }

    public static enum TutorialPageElement {
        TUTORIAL_PAGE_TITLE,
        TUTORIAL_PAGE_IMAGE,
        TUTORIAL_PAGE_DESCR
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_page, container, false);
    }

    public void stylizeTextElement(TutorialPageFragment element, String text, String color) {

    }

    public void changeImage(String src) {

    }
}
