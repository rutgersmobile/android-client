package edu.rutgers.css.Rutgers.fragments.Recreation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.items.RMenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenu.RMenuRow;
import edu.rutgers.css.Rutgers.items.Recreation.Facility;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Facility information screen
 */
public class RecreationDisplay extends Fragment {

    private static final String TAG = "RecreationDisplay";
    public static final String HANDLE = "recdisplay";

    private static final String ID_KEY = "id";
    private static final int ADDRESS_ROW = 0;
    private static final int INFO_ROW = 1;
    private static final int BUSINESS_ROW = 2;
    private static final int DESCRIPTION_ROW = 3;
    private static final int HOURS_ROW = 4;

    private Facility mFacility;
    private RMenuAdapter mAdapter;
    
    public RecreationDisplay() {
        // Required empty public constructor
    }    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        List<RMenuRow> data = new ArrayList<RMenuRow>(10);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, data);

        // Make sure necessary arguments were given
        if(args.getString("campus") == null || args.getString("facility") == null) {
            Log.e(TAG, "Missing campus/location arguments");
            return;
        }

        // Get the facility info
        final String campusTitle = args.getString("campus");
        final String facilityTitle = args.getString("facility");

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Gyms.getFacility(campusTitle, facilityTitle)).done(new DoneCallback<Facility>() {
            @Override
            public void onDone(@NonNull Facility result) {
                mFacility = result;
                addInfo();
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtil.showFailedLoadToast(getActivity());
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_recreation_display, parent, false);

        final Bundle args = getArguments();
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));

        // Make sure necessary arguments were given
        if(args.getString("campus") == null || args.getString("facility") == null) {
            AppUtil.showFailedLoadToast(getActivity());
            return v;
        }

        // When view is recreated, populate list if we have facility info
        if(mAdapter.isEmpty() && mFacility != null) addInfo();

        // Set up list adapter
        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clicked = (RMenuItemRow) parent.getItemAtPosition(position);
                Bundle clickedArgs = clicked.getArgs();

                switch(clickedArgs.getInt(ID_KEY)) {
                    case ADDRESS_ROW:
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                        mapIntent.setData(Uri.parse("geo:0,0?q=" + clicked.getTitle()));
                        try {
                            startActivity(mapIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case INFO_ROW:
                    case BUSINESS_ROW:
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                        dialIntent.setData(Uri.parse("tel:"+clicked.getTitle()));
                        try {
                            startActivity(dialIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DESCRIPTION_ROW:
                        Bundle descArgs = new Bundle(clickedArgs);
                        descArgs.putString("title", args.getString("title"));
                        descArgs.putString("component", TextDisplay.HANDLE);
                        ComponentFactory.getInstance().switchFragments(descArgs);
                        break;

                    case HOURS_ROW:
                        Bundle hoursArgs = new Bundle(clickedArgs);
                        hoursArgs.putString("title", args.getString("title") + " - Hours");
                        hoursArgs.putString("component", RecreationHoursDisplay.HANDLE);
                        ComponentFactory.getInstance().switchFragments(hoursArgs);
                        break;
                }
            }
        });

        return v;
    }

    private void addInfo() {
        // If resources aren't available when callback fires, exit
        if(mFacility == null || getResources() == null) return;

        // Fill in location info
        String infoDesk = mFacility.getInformationNumber();
        String businessOffice = mFacility.getBusinessNumber();
        String address = mFacility.getAddress();
        String descriptionHtml =  StringEscapeUtils.unescapeHtml4(mFacility.getFullDescription());

        // Add "View Hours" row
        if(mFacility.getDaySchedules() != null && !mFacility.getDaySchedules().isEmpty()) {
            try {
                Bundle rowArgs = new Bundle();
                rowArgs.putInt(ID_KEY, HOURS_ROW);
                rowArgs.putString("title", getString(R.string.rec_view_hours));
                rowArgs.putSerializable(RecreationHoursDisplay.DATA_TAG, (Serializable) mFacility.getDaySchedules());

                mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_hours_header)));
                mAdapter.add(new RMenuItemRow(rowArgs));
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "addInfo(): " + e.getMessage());
            }
        }

        // Add "Description" row
        if(StringUtils.isNotBlank(descriptionHtml)) {
            // Decode HTML chars, remove HTML tags, remove whitespace from beginning and end, and
            // then ellipsize the description for the description preview.
            String desc = StringEscapeUtils.unescapeHtml4(descriptionHtml);
            desc = AppUtil.stripTags(desc);
            desc = StringUtils.strip(desc);
            desc = StringUtils.abbreviate(desc, 100);

            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, DESCRIPTION_ROW);
            rowArgs.putString("title", desc);
            rowArgs.putString("data", descriptionHtml);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_description_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if(StringUtils.isNotBlank(address)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, ADDRESS_ROW);
            rowArgs.putString("title", address);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_address_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if(StringUtils.isNotBlank(infoDesk)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, INFO_ROW);
            rowArgs.putString("title", infoDesk);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_info_desk_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if(StringUtils.isNotBlank(businessOffice)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, BUSINESS_ROW);
            rowArgs.putString("title", businessOffice);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_business_office_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

    }

}
