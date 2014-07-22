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

	private static final String TAG = "RUInfoMain";
	
	// Can possibly grab these from a resource file or API later on
	private String mPhoneNumber = "tel:7324454636";
	private String mTextNumber = "66746";
	private String mEmailAddress = "colhenry@rci.rutgers.edu";
	
	public RUInfoMain() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ruinfo_main, container, false);
		
		getActivity().setTitle(getResources().getString(R.string.ruinfo_title));
		
		final Button callButton = (Button) v.findViewById(R.id.button1);
		final Button textButton1 = (Button) v.findViewById(R.id.button2);
		final Button textButton2 = (Button) v.findViewById(R.id.button3);
		final Button emailButton = (Button) v.findViewById(R.id.button4);
		
		// "Call RU-info"
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(mPhoneNumber));
				startActivity(intent);
			}
			
		});
		
		// "Text Rutgers to 66746"
		textButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setType("vnd.android-dir/mms-sms");
				intent.putExtra("address", mTextNumber);
				intent.putExtra("sms_body", "Rutgers");
				startActivity(intent);
			}
			
		});

		// "Text RU-info"
		textButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setType("vnd.android-dir/mms-sms");
				intent.putExtra("address", mTextNumber);
				startActivity(intent);
			}
			
		});
		
		// "Email Colonel Henry"
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailAddress});
				startActivity(intent);
			}
			
		});
		
		return v;
	}
	
}
