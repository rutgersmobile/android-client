package edu.rutgers.css.Rutgers.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers2.R;

public class MainScreen extends Fragment {

    public MainScreen() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_main_screen, container, false);
        
        getActivity().setTitle(R.string.app_name);

        // Set background after view is created and size is set
        v.post(new Runnable() {
            @Override
            public void run() {
                if(!isAdded()) return;
                // Loads and resizes the background logo
                Bitmap bg = ImageUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.background, v.getWidth(), v.getHeight());
                Drawable bgDrawable = new BitmapDrawable(getResources(), bg);

                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    v.setBackground(bgDrawable);
                } else {
                    v.setBackgroundDrawable(bgDrawable);
                }
            }
        });

        return v;
    }

}
