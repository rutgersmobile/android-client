package edu.rutgers.css.Rutgers.channels.recreation.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.recreation.model.Facility;
import edu.rutgers.css.Rutgers.channels.recreation.model.FacilityDaySchedule;
import edu.rutgers.css.Rutgers.channels.recreation.model.GymsAPI;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.TextDisplay;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Facility information: hours, address, phone numbers, etc.
 */
public class RecreationDisplay extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "RecreationDisplay";
    public static final String HANDLE               = "recdisplay";

    /* Argument bundle tags */
    public static final String ARG_TITLE_TAG        = ComponentFactory.ARG_TITLE_TAG;
    public static final String ARG_CAMPUS_TAG       = "campus";
    public static final String ARG_FACILITY_TAG     = "facility";

    /* Constants */
    private static final String ID_KEY = "rec.row.id";
    private static final int ADDRESS_ROW = 0;
    private static final int INFO_ROW = 1;
    private static final int BUSINESS_ROW = 2;
    private static final int DESCRIPTION_ROW = 3;
    private static final int HOURS_ROW = 4;

    /* Member data */
    private Facility mFacility;
    private RMenuAdapter mAdapter;
    private boolean mLoading;
    
    public RecreationDisplay() {
        // Required empty public constructor
    }    

    /** Create argument bundle for a facility information display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String campus, @NonNull String facility) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, RecreationDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_CAMPUS_TAG, campus);
        bundle.putString(ARG_FACILITY_TAG, facility);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        List<RMenuRow> data = new ArrayList<>(10);
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, data);

        // Make sure necessary arguments were given
        if (args.getString(ARG_CAMPUS_TAG) == null || args.getString(ARG_FACILITY_TAG) == null) {
            LOGE(TAG, "Missing campus/location arguments");
            return;
        }

        // Get the facility info
        final String campusTitle = args.getString(ARG_CAMPUS_TAG);
        final String facilityTitle = args.getString(ARG_FACILITY_TAG);

        mLoading = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(GymsAPI.getFacility(campusTitle, facilityTitle)).done(new DoneCallback<Facility>() {
            @Override
            public void onDone(@NonNull Facility result) {
                mFacility = result;
                addInfo();
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }
        }).always(new AlwaysCallback<Facility, Exception>() {
            @Override
            public void onAlways(Promise.State state, Facility resolved, Exception rejected) {
                mLoading = false;
                hideProgressCircle();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_progress);

        if (mLoading) showProgressCircle();

        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));

        // Make sure necessary arguments were given
        if (args.getString(ARG_CAMPUS_TAG) == null || args.getString(ARG_FACILITY_TAG) == null) {
            AppUtils.showFailedLoadToast(getActivity());
            return v;
        }

        // When view is recreated, populate list if we have facility info
        if (mAdapter.isEmpty() && mFacility != null) addInfo();

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
                        Bundle descArgs = TextDisplay.createArgs(args.getString(ARG_TITLE_TAG), clickedArgs.getString("data"));
                        switchFragments(descArgs);
                        break;

                    case HOURS_ROW:
                        Bundle hoursArgs = RecreationHoursDisplay.createArgs(args.getString(ARG_TITLE_TAG) + " - Hours",
                                (List<FacilityDaySchedule>) clickedArgs.getSerializable("data"));
                        switchFragments(hoursArgs);
                        break;
                }
            }
        });

        return v;
    }

    private void addInfo() {
        // If resources aren't available when callback fires, exit
        if (mFacility == null || !isAdded() || getResources() == null) return;

        // Fill in location info
        String infoDesk = mFacility.getInformationNumber();
        String businessOffice = mFacility.getBusinessNumber();
        String address = mFacility.getAddress();
        String descriptionHtml =  StringEscapeUtils.unescapeHtml4(mFacility.getFullDescription());

        // Add "View Hours" row
        if (mFacility.getDailySchedules() != null && !mFacility.getDailySchedules().isEmpty()) {
            try {
                Bundle rowArgs = new Bundle();
                rowArgs.putInt(ID_KEY, HOURS_ROW);
                rowArgs.putString("title", getString(R.string.rec_view_hours));
                rowArgs.putSerializable("data", (Serializable) mFacility.getDailySchedules());

                mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_hours_header)));
                mAdapter.add(new RMenuItemRow(rowArgs));
            } catch (JsonSyntaxException e) {
                LOGE(TAG, "addInfo(): " + e.getMessage());
            }
        }

        // Add "Description" row
        if (StringUtils.isNotBlank(descriptionHtml)) {
            // Decode HTML chars, remove HTML tags, remove whitespace from beginning and end, and
            // then ellipsize the description for the description preview.
            String desc = StringEscapeUtils.unescapeHtml4(descriptionHtml);
            desc = AppUtils.stripTags(desc);
            desc = StringUtils.strip(desc);
            desc = StringUtils.abbreviate(desc, 100);

            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, DESCRIPTION_ROW);
            rowArgs.putString("title", desc);
            rowArgs.putString("data", descriptionHtml);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_description_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if (StringUtils.isNotBlank(address)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, ADDRESS_ROW);
            rowArgs.putString("title", address);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_address_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if (StringUtils.isNotBlank(infoDesk)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, INFO_ROW);
            rowArgs.putString("title", infoDesk);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_info_desk_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

        if (StringUtils.isNotBlank(businessOffice)) {
            Bundle rowArgs = new Bundle();
            rowArgs.putInt(ID_KEY, BUSINESS_ROW);
            rowArgs.putString("title", businessOffice);

            mAdapter.add(new RMenuHeaderRow(getString(R.string.rec_business_office_header)));
            mAdapter.add(new RMenuItemRow(rowArgs));
        }

    }

}
