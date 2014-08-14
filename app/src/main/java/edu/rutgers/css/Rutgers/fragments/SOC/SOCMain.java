package edu.rutgers.css.Rutgers.fragments.SOC;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers2.BuildConfig;
import edu.rutgers.css.Rutgers2.SettingsActivity;
import edu.rutgers.css.Rutgers.adapters.ScheduleAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

public class SOCMain extends Fragment implements SOCDialogFragment.SOCDialogListener {

    private static final String TAG = "SOCMain";

    private List<JSONObject> mData;
    private ScheduleAdapter mAdapter;
    private JSONArray mSemesters;
    private String mDefaultSemester;
    private String mSemester;
    private String mCampus;
    private String mLevel;

    public SOCMain() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();

        setHasOptionsMenu(true);

        mData = new ArrayList<JSONObject>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, mData);

        // Load up schedule settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultSettings(sharedPref);
        mLevel = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_LEVEL, Schedule.CODE_LEVEL_UNDERGRAD);
        mCampus = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_CAMPUS, Schedule.CODE_CAMPUS_NB);
        mSemester = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_SEMESTER, null);

        // Get the available & current semesters
        Schedule.getSemesters().done(new AndroidDoneCallback<JSONObject>() {
            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONObject result) {

                try {
                    JSONArray semesters = result.getJSONArray("semesters");
                    int defaultSemester = result.getInt("defaultSemester");

                    mDefaultSemester = semesters.getString(defaultSemester);
                    mSemesters = semesters;

                    // If there is a saved semester setting, make sure it's valid
                    if(mSemester != null) {
                        boolean iTrulyLoveJSON = false;
                        for(int i = 0; i < semesters.length(); i++) {
                            // Found it
                            if(mSemester.equals(semesters.optString(i))) iTrulyLoveJSON = true;
                        }
                        if(!iTrulyLoveJSON) mSemester = mDefaultSemester;
                    }
                    else mSemester = mDefaultSemester;

                    if(BuildConfig.DEBUG) {
                        for (int i = 0; i < semesters.length(); i++) {
                            Log.v(TAG, "Got semester: " + Schedule.translateSemester(semesters.getString(i)));
                        }
                        Log.v(TAG, "Default semester: " + Schedule.translateSemester(semesters.getString(defaultSemester)));
                    }

                    // Campus, level, and semester have been set.
                    loadSubjects();
                } catch (JSONException e) {
                    Log.w(TAG, "getSemesters(): " + e.getMessage());
                }
            }
        }).fail(new AndroidFailCallback<AjaxStatus>() {
            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onFail(AjaxStatus result) {
                AppUtil.showFailedLoadToast(getActivity());
            }
        });

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_soc_main, parent, false);
        Resources res = getResources();
        Bundle args = getArguments();

        setTitle();

        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        final ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject clickedJSON = (JSONObject) parent.getItemAtPosition(position);
                Bundle args = new Bundle();
                args.putString("campus", mCampus);
                args.putString("semester", mSemester);
                args.putString("level", mLevel);

                if (!clickedJSON.has("courseNumber")) {
                    args.putString("component", "soccourses");
                    args.putString("title", clickedJSON.optString("description") + " (" + clickedJSON.optString("code") + ")");
                    args.putString("subjectCode", clickedJSON.optString("code"));
                } else {
                    // This is for when a course is clicked if it comes up through special filter
                    args.putString("component", "socsections");
                    args.putString("title", clickedJSON.optString("courseNumber") + ": " + clickedJSON.optString("title"));
                    args.putString("data", clickedJSON.toString());
                }

                ComponentFactory.getInstance().switchFragments(args);
            }

        });

        // Search text listener
        filterEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set filter for list adapter
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Search clear button listener
        filterClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEditText.setText("");
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.soc_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle options button
        if(item.getItemId() == R.id.action_options) {
            showSelectDialog();
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSettingsSaved() {
        // When settings are saved from dialog, reload schedule
        if(isAdded() && mAdapter != null) {
            Log.v(TAG, "Loading new settings");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mCampus = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_CAMPUS, Schedule.CODE_CAMPUS_NB);
            mLevel = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_LEVEL, Schedule.CODE_LEVEL_UNDERGRAD);
            mSemester = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_SEMESTER, mDefaultSemester);
            loadSubjects();
        }
    }

    /**
     * Set title based on current campus, semester, and level configuration.
     */
    private void setTitle() {
        getActivity().setTitle(mCampus + " " + Schedule.translateSemester(mSemester) + " " + mLevel);
    }

    /**
     * Show dialog for choosing semester, campus, level.
     */
    private void showSelectDialog() {
        if(mSemesters == null) {
            Log.e(TAG, "No list of semesters to display for dialog");
            return;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ArrayList<String> semestersList = new ArrayList<String>(5);
        for(int i = 0; i < mSemesters.length(); i++) {
            semestersList.add(mSemesters.optString(i));
        }

        DialogFragment newDialogFragment = SOCDialogFragment.newInstance(this, semestersList);
        newDialogFragment.show(ft, "dialog");
    }

    /**
     * Load list of subjects based on current configuration for campus, level, and semester.
     */
    private void loadSubjects() {
        Log.v(TAG, "Loaded subjects - Campus: " + mCampus + "; Level: " + mLevel + "; Semester: " + mSemester);
        setTitle();
        mAdapter.clear();

        Schedule.getSubjects(mCampus, mLevel, mSemester).done(new AndroidDoneCallback<JSONArray>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onDone(JSONArray result) {
                mAdapter.clear();
                for (int i = 0; i < result.length(); i++) {
                    try {
                        mAdapter.add(result.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.w(TAG, "getSubjects(): " + e.getMessage());
                    }
                }
            }

        }).fail(new AndroidFailCallback<AjaxStatus>() {

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.UI;
            }

            @Override
            public void onFail(AjaxStatus result) {
                AppUtil.showFailedLoadToast(getActivity());
            }

        });
    }

    /**
     * Set default campus & level settings (if necessary) using the configured user role & campus.
     * @param sharedPref Default shared preferences
     */
    private void defaultSettings(SharedPreferences sharedPref) {
        // If there are already prefs, exit
        if(sharedPref.contains(SettingsActivity.KEY_PREF_SOC_LEVEL)) return;

        String campus, level;
        Resources res = getResources();

        // Set default values for schedule preferences if nothing
        String userHome = sharedPref.getString(SettingsActivity.KEY_PREF_HOME_CAMPUS, res.getString(R.string.campus_nb_tag));
        String userLevel = sharedPref.getString(SettingsActivity.KEY_PREF_USER_TYPE, res.getString(R.string.role_undergrad_tag));

        // Pick default campus code based on prefs (fall back to New Brunswick)
        if(userHome.equals(res.getString(R.string.campus_nb_tag))) campus = Schedule.CODE_CAMPUS_NB;
        else if(userHome.equals(res.getString(R.string.campus_nwk_tag))) campus = Schedule.CODE_CAMPUS_NWK;
        else if(userHome.equals(res.getString(R.string.campus_cam_tag))) campus = Schedule.CODE_CAMPUS_CAM;
        else campus = Schedule.CODE_CAMPUS_NB;

        // Pick default user-level code based on prefs (fall back to Undergrad)
        if(userLevel.equals(res.getString(R.string.role_undergrad_tag))) level = Schedule.CODE_LEVEL_UNDERGRAD;
        else if(userLevel.equals(res.getString(R.string.role_grad_tag))) level = Schedule.CODE_LEVEL_GRAD;
        else level = Schedule.CODE_LEVEL_UNDERGRAD;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SettingsActivity.KEY_PREF_SOC_CAMPUS, campus);
        editor.putString(SettingsActivity.KEY_PREF_SOC_LEVEL, level);
        editor.commit();
    }

}