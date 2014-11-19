package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.content.SharedPreferences;
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

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
import edu.rutgers.css.Rutgers.channels.soc.model.Subject;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers2.BuildConfig;
import edu.rutgers.css.Rutgers2.R;

/**
 * Schedule of Classes channel main screen. Lists subjects/departments in catalogue.
 */
public class SOCMain extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SOCMain";
    public static final String HANDLE = "soc";

    private SOCIndex mSOCIndex;
    private ScheduleAdapter mAdapter;
    private List<String> mSemesters;
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

        List<ScheduleAdapterItem> data = new ArrayList<>();
        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, data);

        // Load up schedule settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultSettings(sharedPref);
        mLevel = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
        mCampus = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
        mSemester = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null);

        // Register settings listener
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Restore filter
        if(savedInstanceState != null) {
            mFilterString = savedInstanceState.getString("filter");
        }

        // Get the available & current semesters
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(ScheduleAPI.getSemesters()).done(new DoneCallback<Semesters>() {
            @Override
            public void onDone(Semesters result) {
                int defaultIndex = result.getDefaultSemester();
                List<String> semesters = result.getSemesters();

                if(semesters.isEmpty()) {
                    Log.e(TAG, "Semesters list is empty");
                    return;
                }
                if(defaultIndex < 0 || defaultIndex >= semesters.size()) {
                    Log.w(TAG, "Invalid default index " + defaultIndex);
                    defaultIndex = 0;
                }

                mDefaultSemester = semesters.get(defaultIndex);
                mSemesters = semesters;

                // If there is a saved semester setting, make sure it's valid
                if(mSemester == null || !mSemesters.contains(mSemester)) {
                    mSemester = mDefaultSemester;
                }

                if(BuildConfig.DEBUG) {
                    for(String semester: mSemesters) {
                        Log.v(TAG, "Got semester: " + ScheduleAPI.translateSemester(semester));
                    }
                    Log.v(TAG, "Default semester: " + ScheduleAPI.translateSemester(mDefaultSemester));
                }

                // Campus, level, and semester have been set.
                loadSubjects();

            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
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
                ScheduleAdapterItem clickedItem = (ScheduleAdapterItem) parent.getItemAtPosition(position);
                Bundle args = new Bundle();
                args.putString("campus", mCampus);
                args.putString("semester", mSemester);
                args.putString("level", mLevel);

                if (clickedItem instanceof Subject) {
                    args.putString("component", SOCCourses.HANDLE);
                    args.putString("title", clickedItem.getDisplayTitle());
                    args.putString("subjectCode", ((Subject) clickedItem).getCode());
                    ComponentFactory.getInstance().switchFragments(args);
                } else if(clickedItem instanceof Course && !((Course) clickedItem).isStub()) {
                    // This is for when a course is clicked if it comes up through special filter
                    args.putString("component", SOCSections.HANDLE);
                    args.putString("title", clickedItem.getDisplayTitle());
                    args.putString("data", new Gson().toJson((Course)clickedItem));
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
                filterEditText.setText(null);
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
        if(key.equals(PrefUtils.KEY_PREF_SOC_CAMPUS)
                || key.equals(PrefUtils.KEY_PREF_SOC_LEVEL)
                || key.equals(PrefUtils.KEY_PREF_SOC_SEMESTER)) {


            String temp;

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
            if(!mCampus.equals(temp)) {
                somethingChanged = true;
                mCampus = temp;
            }

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
            if(!mLevel.equals(temp)) {
                somethingChanged = true;
                mLevel = temp;
            }

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, mDefaultSemester);
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
        // Only change title if SOC Main fragment or schedule selection dialog is on screen
        if(!isAdded() || !(AppUtils.isOnTop(getActivity(), SOCMain.HANDLE) || AppUtils.isOnTop(getActivity(), SOCDialogFragment.HANDLE))) return;
        if(mSemester == null) getActivity().setTitle(R.string.soc_title);
        else getActivity().setTitle(ScheduleAPI.translateSemester(mSemester) + " " + mCampus + " " + mLevel);
    }

    /**
     * Show dialog for choosing semester, campus, level.
     */
    private void showSelectDialog() {
        if(mSemesters == null) {
            Log.e(TAG, "No list of semesters to display for dialog");
            return;
        }

        ArrayList<String> semestersList = new ArrayList<>(mSemesters);

        DialogFragment newDialogFragment = SOCDialogFragment.newInstance(semestersList);
        ComponentFactory.getInstance().showDialogFragment(newDialogFragment, SOCDialogFragment.HANDLE);
    }

    /**
     * Load list of subjects based on current configuration for campus, level, and semester.
     */
    private void loadSubjects() {
        Log.v(TAG, "Loading subjects - Campus: " + mCampus + "; Level: " + mLevel + "; Semester: " + mSemester);
        setScheduleTitle();
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        // Get index & list of subjects
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(ScheduleAPI.getIndex(mSemester, mCampus, mLevel),  ScheduleAPI.getSubjects(mCampus, mLevel, mSemester)).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                SOCIndex index = (SOCIndex) results.get(0).getResult();
                List<Subject> subjects = (List<Subject>) results.get(1).getResult();

                mSOCIndex = index;
                mAdapter.setFilterIndex(mSOCIndex);

                // Load subjects
                mAdapter.clear();
                mAdapter.addAll(subjects);

                // Re-apply filter
                if (!StringUtils.isEmpty(mFilterString)) {
                    mAdapter.getFilter().filter(mFilterString);
                }
            }

        }).fail(new FailCallback<OneReject>() {

            @Override
            public void onFail(OneReject result) {
                mAdapter.clear();
                AppUtils.showFailedLoadToast(getActivity());
            }

        });

    }

    /**
     * Set default campus & level settings (if necessary) using the configured user role & campus.
     * @param sharedPref Default shared preferences
     */
    private void defaultSettings(SharedPreferences sharedPref) {
        // If there are already prefs, exit
        if(sharedPref.contains(PrefUtils.KEY_PREF_SOC_LEVEL)) return;

        String campus, level;

        // Set default values for schedule preferences if nothing
        String userHome = sharedPref.getString(PrefUtils.KEY_PREF_HOME_CAMPUS, getString(R.string.campus_nb_tag));
        String userLevel = sharedPref.getString(PrefUtils.KEY_PREF_USER_TYPE, getString(R.string.role_undergrad_tag));

        // Pick default campus code based on prefs (fall back to New Brunswick)
        if(userHome.equals(getString(R.string.campus_nb_tag))) campus = ScheduleAPI.CODE_CAMPUS_NB;
        else if(userHome.equals(getString(R.string.campus_nwk_tag))) campus = ScheduleAPI.CODE_CAMPUS_NWK;
        else if(userHome.equals(getString(R.string.campus_cam_tag))) campus = ScheduleAPI.CODE_CAMPUS_CAM;
        else campus = ScheduleAPI.CODE_CAMPUS_NB;

        // Pick default user-level code based on prefs (fall back to Undergrad)
        if(userLevel.equals(getString(R.string.role_undergrad_tag))) level = ScheduleAPI.CODE_LEVEL_UNDERGRAD;
        else if(userLevel.equals(getString(R.string.role_grad_tag))) level = ScheduleAPI.CODE_LEVEL_GRAD;
        else level = ScheduleAPI.CODE_LEVEL_UNDERGRAD;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PrefUtils.KEY_PREF_SOC_CAMPUS, campus);
        editor.putString(PrefUtils.KEY_PREF_SOC_LEVEL, level);
        editor.commit();
    }

}