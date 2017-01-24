package edu.rutgers.css.Rutgers.channels.ruinfo.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

public class RUInfoMain extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "RUInfoMain";
    public static final String HANDLE               = "ruinfo";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Can possibly grab these from a resource file or API later on */
    private String mPhoneNumber = "tel:7324454636";
    private String mTextNumber = "66746";
    private String mEmailAddress = "colhenry@rci.rutgers.edu";
    private String mMobileURL = "http://m.rutgers.edu/ruinfo.html";
    private String mTabletURL = "http://ruinfo.rutgers.edu/";
    
    public RUInfoMain() {
        // Required empty public constructor
    }

    public static Bundle createArgs(@Nullable String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RUInfoMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = createView(inflater, container, savedInstanceState, R.layout.fragment_ruinfo_main);
        hideProgressCircle();

        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.ruinfo_title);

        //final LinearLayout telephonyLayout = (LinearLayout) v.findViewById(R.id.telephonyLayout);
        //final TextView noTelephonyText = (TextView) v.findViewById(R.id.noTelephonyText);

        final Button callButton = (Button) v.findViewById(R.id.callButton);
        final Button textButton1 = (Button) v.findViewById(R.id.smsButton1);
        final Button textButton2 = (Button) v.findViewById(R.id.smsButton2);
        final Button emailButton = (Button) v.findViewById(R.id.emailButton);
        final Button websiteButton = (Button) v.findViewById(R.id.websiteButton);

        // "Call RU-info"
        callButton.setOnClickListener(v1 -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(mPhoneNumber));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
            }
        });
        
        // "Text Rutgers to 66746"
        textButton1.setOnClickListener(v12 -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
            intent.putExtra("address", mTextNumber);
            intent.putExtra("sms_body", "Rutgers");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
            }
        });

        // "Text RU-info"
        textButton2.setOnClickListener(v13 -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
            intent.putExtra("address", mTextNumber);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
            }
        });
        
        // "Email Colonel Henry"
        emailButton.setOnClickListener(v14 -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailAddress});
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
            }
        });

        // "Visit our website"
        websiteButton.setOnClickListener(v15 -> {
            String url = AppUtils.isTablet(getActivity()) ? mTabletURL : mMobileURL;
            Bundle webArgs = WebDisplay.createArgs(getString(R.string.ruinfo_title), url);
            switchFragments(webArgs);
        });

        return v;
    }

    public Link getLink() {
        return null;
    }
}
