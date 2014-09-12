package edu.rutgers.css.Rutgers.fragments.SOC;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.ScheduleAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Schedule;
import edu.rutgers.css.Rutgers.items.SOCIndex;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.BuildConfig;
import edu.rutgers.css.Rutgers2.R;
import edu.rutgers.css.Rutgers2.SettingsActivity;

public class SOCMain extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SOCMain";
    public static final String HANDLE = "soc";

    private List<JSONObject> mData;
    private SOCIndex mSOCIndex;
    private ScheduleAdapter mAdapter;
    private JSONArray mSemesters;
    private String mDefaultSemester;
    private String mSemester;
    private String mCampus;
    private String mLevel;
    private String mFilterString;

    public SOCMain() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mData = new ArrayList<JSONObject>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, mData);

        // Load up schedule settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultSettings(sharedPref);
        mLevel = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_LEVEL, Schedule.CODE_LEVEL_UNDERGRAD);
        mCampus = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_CAMPUS, Schedule.CODE_CAMPUS_NB);
        mSemester = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_SEMESTER, null);

        // Register settings listener
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Restore filter
        if(savedInstanceState != null) {
            mFilterString = savedInstanceState.getString("filter");
        }

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
        setScheduleTitle();

        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);

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
                    args.putString("component", SOCCourses.HANDLE);
                    args.putString("title", Schedule.subjectLine(clickedJSON));
                    args.putString("subjectCode", clickedJSON.optString("code"));
                    ComponentFactory.getInstance().switchFragments(args);
                }
                else if(!clickedJSON.optBoolean("stub")) {
                    // This is for when a course is clicked if it comes up through special filter
                    args.putString("component", SOCSections.HANDLE);
                    args.putString("title", Schedule.courseLine(clickedJSON));
                    args.putString("data", clickedJSON.toString());
                    ComponentFactory.getInstance().switchFragments(args);
                }
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
                mFilterString = s.toString().trim();
                mAdapter.getFilter().filter(mFilterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Search clear button listener
        final ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFilterString != null) outState.putString("filter", mFilterString);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        Log.v(TAG, "Setting changed: " + key);

        // All 3 changes are submitted at once by the dialog. This gets all of the changes at once
        // in order to avoid calling loadSubjects() multiple times each time the config is changed.
        boolean somethingChanged = false;
        if(key.equals(SettingsActivity.KEY_PREF_SOC_CAMPUS)
                || key.equals(SettingsActivity.KEY_PREF_SOC_LEVEL)
                || key.equals(SettingsActivity.KEY_PREF_SOC_SEMESTER)) {


            String temp;

            temp = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_CAMPUS, Schedule.CODE_CAMPUS_NB);
            if(!mCampus.equals(temp)) {
                somethingChanged = true;
                mCampus = temp;
            }

            temp = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_LEVEL, Schedule.CODE_LEVEL_UNDERGRAD);
            if(!mLevel.equals(temp)) {
                somethingChanged = true;
                mLevel = temp;
            }

            temp = sharedPref.getString(SettingsActivity.KEY_PREF_SOC_SEMESTER, mDefaultSemester);
            if(!mSemester.equals(temp)) {
                somethingChanged = true;
                mSemester = temp;
            }
        }

        if(somethingChanged) {
            Log.v(TAG, "Loading new subjects");
            loadSubjects();
        }
    }

    /**
     * Set title based on current campus, semester, and level configuration.
     */
    private void setScheduleTitle() {
        if(!isAdded()) return;
        if(mSemester == null) getActivity().setTitle(R.string.soc_title);
        else getActivity().setTitle(Schedule.translateSemester(mSemester) + " " + mCampus + " " + mLevel);
    }

    /**
     * Show dialog for choosing semester, campus, level.
     */
    private void showSelectDialog() {
        if(mSemesters == null) {
            Log.e(TAG, "No list of semesters to display for dialog");
            return;
        }

        ArrayList<String> semestersList = new ArrayList<String>(5);
        for(int i = 0; i < mSemesters.length(); i++) {
            semestersList.add(mSemesters.optString(i));
        }

        DialogFragment newDialogFragment = SOCDialogFragment.newInstance(semestersList);
        ComponentFactory.getInstance().showDialogFragment(newDialogFragment, SOCDialogFragment.HANDLE);
    }

    /**
     * Load list of subjects based on current configuration for campus, level, and semester.
     */
    private void loadSubjects() {
        Log.v(TAG, "Loading subjects - Campus: " + mCampus + "; Level: " + mLevel + "; Semester: " + mSemester);
        if(isAdded() && AppUtil.isOnTop(SOCMain.HANDLE)) setScheduleTitle();
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        // Get index & list of subjects
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(Schedule.getIndex(mSemester, mCampus, mLevel),  Schedule.getSubjects(mCampus, mLevel, mSemester)).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                SOCIndex index = (SOCIndex) results.get(0).getResult();
                JSONArray subjects = (JSONArray) results.get(1).getResult();

                mSOCIndex = index;
                mAdapter.setFilterIndex(mSOCIndex);

                // Load subjects
                mAdapter.clear();
                for (int i = 0; i < subjects.length(); i++) {
                    try {
                        mAdapter.add(subjects.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.w(TAG, "getSubjects(): " + e.getMessage());
                    }
                }

                // Re-apply filter
                if (mFilterString != null && !mFilterString.isEmpty()) {
                    mAdapter.getFilter().filter(mFilterString);
                }
            }

        }).fail(new FailCallback<OneReject>() {

            @Override
            public void onFail(OneReject result) {
                mAdapter.clear();
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