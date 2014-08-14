package edu.rutgers.css.Rutgers.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

public class RUInfoMain extends Fragment {

	private static final String TAG = "RUInfoMain";
	
	// Can possibly grab these from a resource file or API later on
	private String mPhoneNumber = "tel:7324454636";
	private String mTextNumber = "66746";
	private String mEmailAddress = "colhenry@rci.rutgers.edu";
    private String mMobileURL = "http://m.rutgers.edu/ruinfo.html";
    private String mTabletURL = "http://ruinfo.rutgers.edu/";
	
	public RUInfoMain() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ruinfo_main, container, false);
		
		getActivity().setTitle(R.string.ruinfo_title);

        //final LinearLayout telephonyLayout = (LinearLayout) v.findViewById(R.id.telephonyLayout);
        //final TextView noTelephonyText = (TextView) v.findViewById(R.id.noTelephonyText);

		final Button callButton = (Button) v.findViewById(R.id.callButton);
		final Button textButton1 = (Button) v.findViewById(R.id.smsButton1);
		final Button textButton2 = (Button) v.findViewById(R.id.smsButton2);
		final Button emailButton = (Button) v.findViewById(R.id.emailButton);
        final Button websiteButton = (Button) v.findViewById(R.id.websiteButton);

		// "Call RU-info"
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(mPhoneNumber));
				try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                }
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
				try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                }
			}
			
		});

		// "Text RU-info"
		textButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setType("vnd.android-dir/mms-sms");
				intent.putExtra("address", mTextNumber);
				try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                }
			}
			
		});
		
		// "Email Colonel Henry"
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailAddress});
				try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                }
			}
			
		});

        // "Visit our website"
        websiteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("component", "www");
                args.putString("title", getResources().getString(R.string.ruinfo_title));
                if(AppUtil.isTablet(getActivity())) args.putString("url", mTabletURL);
                else args.putString("url", mMobileURL);
                ComponentFactory.getInstance().switchFragments(args);
            }

        });

		return v;
	}
	
}
