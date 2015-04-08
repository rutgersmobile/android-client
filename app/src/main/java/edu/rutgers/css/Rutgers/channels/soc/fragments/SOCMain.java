package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
import edu.rutgers.css.Rutgers.channels.soc.model.Subject;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Schedule of Classes channel main screen. Lists subjects/departments in catalogue.
 */
public class SOCMain extends BaseChannelFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCMain";
    public static final String HANDLE               = "soc";

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = "filter";

    /* Member data */
    private SOCIndex mSOCIndex;
    private ScheduleAdapter mAdapter;
    private List<String> mSemesters;
    private String mDefaultSemester;
    private String mSemester;
    private String mCampus;
    private String mLevel;
    private String mFilterString;
    private boolean mLoadingSemesters;
    private boolean mLoadingSubjects;

    public SOCMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for SOC subjects/departments display. */
    public static Bundle createArgs() {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCMain.HANDLE);
        return bundle;
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
        if (savedInstanceState != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG);
        }

        // Get the available & current semesters
        mLoadingSemesters = true;
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(ScheduleAPI.getSemesters()).done(new DoneCallback<Semesters>() {
            @Override
            public void onDone(Semesters result) {
                int defaultIndex = result.getDefaultSemester();
                List<String> semesters = result.getSemesters();

                if (semesters.isEmpty()) {
                    LOGE(TAG, "Semesters list is empty");
                    return;
                } else if (defaultIndex < 0 || defaultIndex >= semesters.size()) {
                    LOGW(TAG, "Invalid default index " + defaultIndex);
                    defaultIndex = 0;
                }

                mDefaultSemester = semesters.get(defaultIndex);
                mSemesters = semesters;

                // If there is a saved semester setting, make sure it's valid
                if (mSemester == null || !mSemesters.contains(mSemester)) {
                    mSemester = mDefaultSemester;
                }

                if (BuildConfig.DEBUG) {
                    for (String semester: mSemesters) {
                        LOGV(TAG, "Got semester: " + ScheduleAPI.translateSemester(semester));
                    }
                    LOGV(TAG, "Default semester: " + ScheduleAPI.translateSemester(mDefaultSemester));
                }

                // Campus, level, and semester have been set.
                loadSubjects();

            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                AppUtils.showFailedLoadToast(getActivity());
            }
        }).always(new AlwaysCallback<Semesters, Exception>() {
            @Override
            public void onAlways(Promise.State state, Semesters resolved, Exception rejected) {
                mLoadingSemesters = false;
                if (!mLoadingSubjects) hideProgressCircle();
            }
        });

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_list_progress);
        setScheduleTitle();

        if (mLoadingSemesters || mLoadingSubjects) showProgressCircle();

        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduleAdapterItem clickedItem = (ScheduleAdapterItem) parent.getItemAtPosition(position);

                if (clickedItem instanceof Subject) {
                    Bundle coursesArgs = SOCCourses.createArgs(clickedItem.getDisplayTitle(), mCampus,
                            mSemester, mLevel, ((Subject) clickedItem).getCode());
                    switchFragments(coursesArgs);
                } else if (clickedItem instanceof Course) {
                    // This is for when courses are loaded into the list by user-supplied filter
                    if (((Course) clickedItem).isStub()) return; // Stub course hasn't loaded data yet
                    Bundle courseArgs = SOCSections.createArgs(clickedItem.getDisplayTitle(), mSemester, (Course) clickedItem);
                    switchFragments(courseArgs);
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
        if (item.getItemId() == R.id.action_options) {
            showSelectDialog();
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilterString != null) outState.putString(SAVED_FILTER_TAG, mFilterString);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        LOGV(TAG, "Setting changed: " + key);

        // All 3 changes are submitted at once by the dialog. This gets all of the changes at once
        // in order to avoid calling loadSubjects() multiple times each time the config is changed.
        boolean somethingChanged = false;
        if (key.equals(PrefUtils.KEY_PREF_SOC_CAMPUS)
                || key.equals(PrefUtils.KEY_PREF_SOC_LEVEL)
                || key.equals(PrefUtils.KEY_PREF_SOC_SEMESTER)) {


            String temp;

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
            if (!mCampus.equals(temp)) {
                somethingChanged = true;
                mCampus = temp;
            }

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
            if (!mLevel.equals(temp)) {
                somethingChanged = true;
                mLevel = temp;
            }

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, mDefaultSemester);
            if (!mSemester.equals(temp)) {
                somethingChanged = true;
                mSemester = temp;
            }
        }

        if (somethingChanged) {
            LOGV(TAG, "Loading new subjects");
            loadSubjects();
        }
    }

    /**
     * Set title based on current campus, semester, and level configuration.
     */
    private void setScheduleTitle() {
        // Only change title if SOC Main fragment on screen (possibly covered by selection dialog)
        if (!isAdded() || getResources() == null) return;
        if (!(AppUtils.isOnTop(getActivity(), SOCMain.HANDLE) || AppUtils.isOnTop(getActivity(), SOCDialogFragment.HANDLE))) return;
        if (mSemester == null) getActivity().setTitle(R.string.soc_title);
        else getActivity().setTitle(ScheduleAPI.translateSemester(mSemester) + " " + mCampus + " " + mLevel);
    }

    /**
     * Show dialog for choosing semester, campus, level.
     */
    private void showSelectDialog() {
        if (mSemesters == null) {
            LOGE(TAG, "No list of semesters to display for dialog");
            return;
        }

        ArrayList<String> semestersList = new ArrayList<>(mSemesters);

        DialogFragment newDialogFragment = SOCDialogFragment.newInstance(semestersList);
        showDialogFragment(newDialogFragment, SOCDialogFragment.HANDLE);
    }

    /**
     * Load list of subjects based on current configuration for campus, level, and semester.
     */
    private void loadSubjects() {
        LOGV(TAG, "Loading subjects - Campus: " + mCampus + "; Level: " + mLevel + "; Semester: " + mSemester);
        setScheduleTitle();
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        // Get index & list of subjects
        mLoadingSubjects = true;
        showProgressCircle();
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

        }).always(new AlwaysCallback<MultipleResults, OneReject>() {
            @Override
            public void onAlways(Promise.State state, MultipleResults resolved, OneReject rejected) {
                mLoadingSubjects = false;
                if (!mLoadingSemesters) hideProgressCircle();
            }
        });

    }

    /**
     * Set default campus & level settings (if necessary) using the configured user role & campus.
     * @param sharedPref Default shared preferences
     */
    private void defaultSettings(SharedPreferences sharedPref) {
        // If there are already prefs, exit
        if (sharedPref.contains(PrefUtils.KEY_PREF_SOC_LEVEL)) return;

        String campus, level;

        // Set default values for schedule preferences if nothing
        String userHome = sharedPref.getString(PrefUtils.KEY_PREF_HOME_CAMPUS, getString(R.string.campus_nb_tag));
        String userLevel = sharedPref.getString(PrefUtils.KEY_PREF_USER_TYPE, getString(R.string.role_undergrad_tag));

        // Pick default campus code based on prefs (fall back to New Brunswick)
        if (userHome.equals(getString(R.string.campus_nb_tag))) campus = ScheduleAPI.CODE_CAMPUS_NB;
        else if (userHome.equals(getString(R.string.campus_nwk_tag))) campus = ScheduleAPI.CODE_CAMPUS_NWK;
        else if (userHome.equals(getString(R.string.campus_cam_tag))) campus = ScheduleAPI.CODE_CAMPUS_CAM;
        else campus = ScheduleAPI.CODE_CAMPUS_NB;

        // Pick default user-level code based on prefs (fall back to Undergrad)
        if (userLevel.equals(getString(R.string.role_undergrad_tag))) level = ScheduleAPI.CODE_LEVEL_UNDERGRAD;
        else if (userLevel.equals(getString(R.string.role_grad_tag))) level = ScheduleAPI.CODE_LEVEL_GRAD;
        else level = ScheduleAPI.CODE_LEVEL_UNDERGRAD;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PrefUtils.KEY_PREF_SOC_CAMPUS, campus);
        editor.putString(PrefUtils.KEY_PREF_SOC_LEVEL, level);
        editor.commit();
    }

}