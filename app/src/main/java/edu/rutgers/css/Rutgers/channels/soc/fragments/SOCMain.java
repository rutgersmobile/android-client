package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapterItem;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
import edu.rutgers.css.Rutgers.channels.soc.model.Subject;
import edu.rutgers.css.Rutgers.channels.soc.model.loader.SubjectLoader;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Schedule of Classes channel main screen. Lists subjects/departments in catalogue.
 */
public class SOCMain extends BaseChannelFragment implements SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<SubjectLoader.SubjectHolder> {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCMain";
    public static final String HANDLE               = "soc";

    private static final int LOADER_ID              = 1;

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
    private boolean mLoading;

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

        mAdapter = new ScheduleAdapter(getActivity(), R.layout.row_course, R.layout.row_section_header);

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

        mLoading = true;
        showProgressCircle();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_stickylist_progress);
        setScheduleTitle();

        if (mLoading) showProgressCircle();

        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduleAdapterItem clickedItem = (ScheduleAdapterItem) parent.getItemAtPosition(position);

                if (clickedItem instanceof Subject) {
                    Bundle coursesArgs = SOCCourses.createArgs(clickedItem.getDisplayTitle(), mCampus,
                            mSemester, mLevel, clickedItem.getCode());
                    switchFragments(coursesArgs);
                } else if (clickedItem instanceof Course) {
                    // This is for when courses are loaded into the list by user-supplied filter
                    final Course course = (Course) clickedItem;
                    Bundle courseArgs = SOCSections.createArgs(
                            course.getDisplayTitle(), mSemester, mSOCIndex.getCampusCode(),
                            mSOCIndex.getSemesterCode(), course.getSubject(),
                            course.getCourseNumber()
                    );
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
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    /**
     * Set title based on current campus, semester, and level configuration.
     */
    private void setScheduleTitle() {
        // Only change title if SOC Main fragment on screen (possibly covered by selection dialog)
        if (!isAdded() || getResources() == null) return;
        if (!(AppUtils.isOnTop(getActivity(), SOCMain.HANDLE)
                || AppUtils.isOnTop(getActivity(), SOCDialogFragment.HANDLE)
                || ((MainActivity) getActivity()).getFragmentMediator().isFirstVisibleFragment(SOCMain.HANDLE))) return;
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

    @Override
    public Loader<SubjectLoader.SubjectHolder> onCreateLoader(int id, Bundle args) {
        return new SubjectLoader(getContext(), mSemester, mLevel, mCampus);
    }

    @Override
    public void onLoadFinished(Loader<SubjectLoader.SubjectHolder> loader, SubjectLoader.SubjectHolder data) {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();

        SOCIndex socIndex = data.getIndex();
        List<Subject> subjects = data.getSubjects();
        Semesters semesters = data.getSemesters();
        String semester = data.getSemester();
        String defaultSemester = data.getDefaultSemester();

        if (socIndex == null || subjects == null || semesters == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }

        mSemester = semester;
        mDefaultSemester = defaultSemester;

        mSemesters = semesters.getSemesters();

        mSOCIndex = socIndex;
        mAdapter.setFilterIndex(mSOCIndex);

        mAdapter.addAllSubjects(subjects);

        setScheduleTitle();
    }

    @Override
    public void onLoaderReset(Loader<SubjectLoader.SubjectHolder> loader) {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}