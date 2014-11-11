package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.soc.model.Schedule;
import edu.rutgers.css.Rutgers.model.KeyValPair;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers2.R;

/**
 * Schedule of Classes catalogue selection menu.
 */
public class SOCDialogFragment extends DialogFragment {

    private static final String TAG = "SOCDialog";
    public static final String HANDLE = "socconfdialog";

    private SpinnerAdapter mSemesterSpinnerAdapter;
    private SpinnerAdapter mCampusSpinnerAdapter;
    private SpinnerAdapter mLevelSpinnerAdapter;
    private ArrayList<KeyValPair> mSemesters;

    public SOCDialogFragment() {
        // Required public empty constructor
    }

    public static SOCDialogFragment newInstance(List<String> semesters) {
        SOCDialogFragment dialog = new SOCDialogFragment();
        dialog.setSemesters(semesters);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mSemesters = (ArrayList<KeyValPair>) savedInstanceState.getSerializable("semesters");
        }

        ArrayList<KeyValPair> campuses = loadCampuses();

        ArrayList<KeyValPair> levels = new ArrayList<KeyValPair>(2);
        levels.add(new KeyValPair(Schedule.CODE_LEVEL_UNDERGRAD, getString(R.string.soc_undergrad)));
        levels.add(new KeyValPair(Schedule.CODE_LEVEL_GRAD, getString(R.string.soc_grad)));

        mSemesterSpinnerAdapter = new ArrayAdapter<KeyValPair>(getActivity(), android.R.layout.simple_dropdown_item_1line, mSemesters);
        mCampusSpinnerAdapter = new ArrayAdapter<KeyValPair>(getActivity(), android.R.layout.simple_dropdown_item_1line, campuses);
        mLevelSpinnerAdapter = new ArrayAdapter<KeyValPair>(getActivity(), android.R.layout.simple_dropdown_item_1line, levels);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.dialog_soc, null);
        final Spinner semesterSpinner = (Spinner) view.findViewById(R.id.semesterSpinner);
        final Spinner campusSpinner = (Spinner) view.findViewById(R.id.campusSpinner);
        final Spinner levelSpinner = (Spinner) view.findViewById(R.id.levelSpinner);

        semesterSpinner.setAdapter(mSemesterSpinnerAdapter);
        campusSpinner.setAdapter(mCampusSpinnerAdapter);
        levelSpinner.setAdapter(mLevelSpinnerAdapter);

        // Set selections to user's configured options
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setSelectionByConfig(sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, null), mCampusSpinnerAdapter, campusSpinner);
        setSelectionByConfig(sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, null), mLevelSpinnerAdapter, levelSpinner);
        setSelectionByConfig(sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null), mSemesterSpinnerAdapter, semesterSpinner);

        builder.setView(view)
                .setTitle(R.string.soc_select)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Save settings
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(PrefUtils.KEY_PREF_SOC_CAMPUS, ((KeyValPair) campusSpinner.getSelectedItem()).getKey());
                        editor.putString(PrefUtils.KEY_PREF_SOC_LEVEL, ((KeyValPair) levelSpinner.getSelectedItem()).getKey());
                        editor.putString(PrefUtils.KEY_PREF_SOC_SEMESTER, ((KeyValPair) semesterSpinner.getSelectedItem()).getKey());
                        editor.apply();
                        Log.i(TAG, "Saved settings");
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {

                   }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("semesters", mSemesters);
        super.onSaveInstanceState(outState);
    }

    /**
     * Set list of current semesters for the spinner.
     * @param semesterCodes List of semester codes
     */
    public void setSemesters(List<String> semesterCodes) {
        ArrayList<KeyValPair> semesters = new ArrayList<KeyValPair>(5);
        for(String code: semesterCodes) {
            semesters.add(new KeyValPair(code, Schedule.translateSemester(code)));
        }
        mSemesters = semesters;
    }

    /**
     * Load list of campuses and their SOC codes from the JSON resource.
     * @return Array of campus/code key value pairs
     */
    private ArrayList<KeyValPair> loadCampuses() {
        ArrayList<KeyValPair> results = new ArrayList<KeyValPair>();
        JSONArray campusJSONArray = AppUtils.loadRawJSONArray(getResources(), R.raw.soc_campuses);
        if(campusJSONArray == null) {
            Log.e(TAG, "Couldn't get list of campuses for SOC");
            return results;
        }

        for(int i = 0; i < campusJSONArray.length(); i++) {
            try {
                JSONObject campusJSON = campusJSONArray.getJSONObject(i);
                results.add(new KeyValPair(campusJSON.getString("tag"), campusJSON.getString("title")));
            } catch(JSONException e) {
                Log.w(TAG, "loadCampuses(): " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Set selected spinner items based on user's existing config.
     * @param configString Current config value
     * @param spinnerAdapter Spinner adapter to search for value matching config string
     * @param spinner Spinner view to perform selection for
     */
    private void setSelectionByConfig(String configString, SpinnerAdapter spinnerAdapter, Spinner spinner) {
        if(configString == null) return;
        for(int i = 0; i < spinnerAdapter.getCount(); i++) {
            KeyValPair pair = (KeyValPair) spinnerAdapter.getItem(i);
            if(configString.equals(pair.getKey())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

}
