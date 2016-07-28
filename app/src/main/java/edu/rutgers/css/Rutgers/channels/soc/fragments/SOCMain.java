package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.soc.Registerable;
import edu.rutgers.css.Rutgers.api.soc.ScheduleAPI;
import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.api.soc.model.Semesters;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAdapter;
import edu.rutgers.css.Rutgers.channels.soc.model.loader.SubjectLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Schedule of Classes channel main screen. Lists subjects/departments in catalogue.
 */
public class SOCMain extends BaseChannelFragment implements SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<SubjectLoader.SubjectHolder> {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCMain";
    public static final String HANDLE               = "soc";

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = "filter";
    private static final String SEARCHING_TAG       = "searching";

    private static final String ARG_LEVEL_TAG       = "level";
    private static final String ARG_CAMPUS_TAG      = "campus";
    private static final String ARG_SEMESTER_TAG    = "semester";

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
    private EditText filterEditText;
    private boolean searching = false;

    public SOCMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for SOC subjects/departments display. */
    public static Bundle createArgs() {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, SOCMain.HANDLE);
        return bundle;
    }

    public static Bundle createArgs(@NonNull final String level, @NonNull final String campus, @NonNull final String semester) {
        Bundle bundle = createArgs();
        bundle.putString(ARG_LEVEL_TAG, level);
        bundle.putString(ARG_CAMPUS_TAG, campus);
        bundle.putString(ARG_SEMESTER_TAG, semester);
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
        final String prefLevel = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
        final String prefCampus = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
        final String prefSemester = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null);

        final Bundle args = getArguments();
        mLevel = args.getString(ARG_LEVEL_TAG, prefLevel);
        mCampus = args.getString(ARG_CAMPUS_TAG, prefCampus);
        mSemester = args.getString(ARG_SEMESTER_TAG, prefSemester);

        // Register settings listener
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Restore filter
        if (savedInstanceState != null && filterEditText != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG, "");
            searching = savedInstanceState.getBoolean(SEARCHING_TAG);
            filterEditText.setText(mFilterString);
        }

        mLoading = true;
        showProgressCircle();
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSearchUI();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_stickylist_progress,
                CreateArgs.builder().toolbarRes(R.id.toolbar_search).build());

        setScheduleTitle();

        if (mLoading) showProgressCircle();

        filterEditText = (EditText) v.findViewById(R.id.search_box);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Registerable clickedItem = (Registerable) parent.getItemAtPosition(position);

                if (clickedItem instanceof Subject) {
                    Bundle coursesArgs = SOCCourses.createArgs(clickedItem.getDisplayTitle(), mCampus,
                            mSemester, mLevel, clickedItem.getCode());
                    switchFragments(coursesArgs);
                } else if (clickedItem instanceof Course) {
                    // This is for when courses are loaded into the list by user-supplied filter
                    final Course course = (Course) clickedItem;
                    Bundle courseArgs = SOCSections.createArgs(
                            course.getDisplayTitle(), mSemester, mSOCIndex.getCampusCode(),
                            course.getSubject(), course.getCourseNumber()
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

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.soc_menu, menu);
        MenuItem optionsItem = menu.findItem(R.id.action_options);
        MenuItem searchButton = menu.findItem(R.id.search_button_toolbar);

        if (searching) {
            optionsItem.setVisible(false);
            searchButton.setIcon(R.drawable.ic_clear_black_24dp);
        } else {
            optionsItem.setVisible(true);
            searchButton.setIcon(R.drawable.ic_search_white_24dp);
        }
    }

    @Override
    public Link getLink() {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add(mCampus.toLowerCase());
        pathParts.add(mSemester);
        pathParts.add(mLevel.toLowerCase());
        return new Link("soc", pathParts, getLinkTitle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle options button
        if (item.getItemId() == R.id.action_options) {
            showSelectDialog();
            return true;
        } else if (item.getItemId() == R.id.search_button_toolbar) {
            searching = !searching;
            updateSearchUI();
            return true;
        }

        return false;
    }

    public void updateSearchUI() {
        if (searching) {
            filterEditText.setVisibility(View.VISIBLE);
            filterEditText.requestFocus();
            getToolbar().setBackgroundColor(getResources().getColor(R.color.white));
            AppUtils.openKeyboard(getActivity());
        } else {
            filterEditText.setVisibility(View.GONE);
            getToolbar().setBackgroundColor(getResources().getColor(R.color.actbar_new));
            filterEditText.setText("");
            AppUtils.closeKeyboard(getActivity());
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilterString != null) outState.putString(SAVED_FILTER_TAG, mFilterString);
        outState.putBoolean(SEARCHING_TAG, true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        LOGV(TAG, "Setting changed: " + key);

        if (!isAdded()) return;

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
            getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }
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
        editor.apply();
    }

    @Override
    public Loader<SubjectLoader.SubjectHolder> onCreateLoader(int id, Bundle args) {
        return new SubjectLoader(getContext(), mSemester, mLevel, mCampus);
    }

    @Override
    public void onLoadFinished(Loader<SubjectLoader.SubjectHolder> loader, SubjectLoader.SubjectHolder data) {
        reset();

        final SOCIndex socIndex = data.getIndex();
        final List<Subject> subjects = data.getSubjects();
        final Semesters semesters = data.getSemesters();
        final String semester = data.getSemester();
        final String defaultSemester = data.getDefaultSemester();

        // all of these values will be filled out if there is not an error
        if (socIndex == null || subjects == null || semesters == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }

        // Set all the values we got back

        mSemester = semester;
        mDefaultSemester = defaultSemester;

        mSemesters = semesters.getSemesters();

        // the index has important information for searching so we
        // get it during this load
        mSOCIndex = socIndex;
        mAdapter.setFilterIndex(mSOCIndex);

        mAdapter.addAllSubjects(subjects);
        setScheduleTitle();
    }

    private void setScheduleTitle() {
        if (mSemester != null && mCampus != null && mLevel != null) {
            final String title = ScheduleAPI.translateSemester(mSemester) + " " + mCampus + " " + mLevel;
            getActivity().setTitle(title);
        }
    }

    @Override
    public void onLoaderReset(Loader<SubjectLoader.SubjectHolder> loader) {
        reset();
    }

    private void reset() {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}