package edu.rutgers.css.Rutgers.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import edu.rutgers.css.Rutgers2.R;

public class RUInfoMain extends Fragment {

	public RUInfoMain() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ruinfo_main, container, false);
		
		getActivity().setTitle(getActivity().getResources().getString(R.string.ruinfo_title));
		
		final Button callButton = (Button) v.findViewById(R.id.button1);
		final Button textButton1 = (Button) v.findViewById(R.id.button2);
		final Button textButton2 = (Button) v.findViewById(R.id.button3);
		final Button emailButton = (Button) v.findViewById(R.id.button4);
		
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent(v);
			}
			
		});
		
		textButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				textIntent(v);
			}
			
		});

		textButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				textIntent(v);
			}
			
		});
		
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				emailIntent(v);
			}
			
		});
		
		return v;
	}
	
	public void callIntent(View v) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:7324454636"));
		startActivity(intent);
	}

	public void textIntent(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setType("vnd.android-dir/mms-sms");
		intent.putExtra("address", "66746");
		
		// Clicked "text Rutgers to..."
		if(v.getId() == R.id.button2) {
			intent.putExtra("sms_body", "Rutgers");
		}
		
		startActivity(intent);
	}
	
	public void emailIntent(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"colhenry@rci.rutgers.edu"});
		startActivity(intent);
	}
	
}
