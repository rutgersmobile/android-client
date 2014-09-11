package edu.rutgers.css.Rutgers.fragments;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers2.R;

public class MainScreen extends Fragment {

	public MainScreen() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_main_screen, container, false);
		
		getActivity().setTitle(R.string.app_name);

        int bgResource;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) bgResource = R.drawable.bgland;
        else bgResource = R.drawable.bgportrait;
        Bitmap bg = BitmapFactory.decodeResource(getResources(), bgResource, new BitmapFactory.Options());
        Drawable bgDraw = new BitmapDrawable(bg);
        v.setBackground(bgDraw);
		
		return v;
	}
}
