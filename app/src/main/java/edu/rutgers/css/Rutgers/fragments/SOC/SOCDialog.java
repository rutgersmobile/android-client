package edu.rutgers.css.Rutgers.fragments.SOC;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers2.R;

/**
 * Created by jamchamb on 8/11/14.
 */
public class SOCDialog extends DialogFragment {

    private SpinnerAdapter mSemesterSpinnerAdapter;
    private SpinnerAdapter mCampusSpinnerAdapter;
    private SpinnerAdapter mLevelSpinnerAdapter;
    private List<String> mSemesters;

    public SOCDialog() {
        // Required public empty constructor
    }

    public static SOCDialog newInstance(List<String> semesters) {
        SOCDialog dialog = new SOCDialog();
        dialog.setSemesters(semesters);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] campusList = new String[]{"New Brunswick", "Newark", "Camden"};
        String[] levelList = new String[]{"Undergraduate", "Graduate"};
        mSemesterSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, mSemesters);
        mCampusSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, campusList);
        mLevelSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, levelList);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_soc, container, false);

        getDialog().setTitle(R.string.soc_select);

        final Spinner semesterSpinner = (Spinner) v.findViewById(R.id.semesterSpinner);
        final Spinner campusSpinner = (Spinner) v.findViewById(R.id.campusSpinner);
        final Spinner levelSpinner = (Spinner) v.findViewById(R.id.levelSpinner);

        semesterSpinner.setAdapter(mSemesterSpinnerAdapter);
        campusSpinner.setAdapter(mCampusSpinnerAdapter);
        levelSpinner.setAdapter(mLevelSpinnerAdapter);

        return v;
    }

    public void setSemesters(List<String> semesters) {
        mSemesters = semesters;
    }

}
