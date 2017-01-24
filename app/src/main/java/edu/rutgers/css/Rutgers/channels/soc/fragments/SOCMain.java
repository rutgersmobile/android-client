package edu.rutgers.css.Rutgers.channels.soc.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.RutgersAPI;
import edu.rutgers.css.Rutgers.api.SOCAPI;
import edu.rutgers.css.Rutgers.api.model.soc.Course;
import edu.rutgers.css.Rutgers.api.model.soc.SOCIndex;
import edu.rutgers.css.Rutgers.api.model.soc.Semesters;
import edu.rutgers.css.Rutgers.api.model.soc.Subject;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.SectionedScheduleAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGV;

/**
 * Schedule of Classes channel main screen. Lists subjects/departments in catalogue.
 */
public class SOCMain
    extends BaseChannelFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    /* Log tag and component handle */
    private static final String TAG                 = "SOCMain";
    public static final String HANDLE               = "soc";

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = "filter";
    private static final String SEARCHING_TAG       = "searching";

    private static final String ARG_LEVEL_TAG       = "level";
    private static final String ARG_CAMPUS_TAG      = "campus";
    private static final String ARG_SEMESTER_TAG    = "semester";

    /* Member data */
    private SOCIndex mSOCIndex;
    private SectionedScheduleAdapter mAdapter;
    private List<String> mSemesters;
    private String mDefaultSemester;
    private String mSemester;
    private String mCampus;
    private String mLevel;
    private String mFilterString;
    private EditText filterEditText;
    private boolean searching = false;

    private final PublishSubject<ScheduleArgHolder> argHolderPublishSubject = PublishSubject.create();

    @Override
    public String getLogTag() {
        return TAG;
    }

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

    public static class SubjectHolder {
        private final SOCIndex index;
        private final List<Subject> subjects;
        private final Semesters semesters;
        private final String semester;
        private final String defaultSemester;

        public SubjectHolder(final SOCIndex index, final List<Subject> subjects, final Semesters semesters,
                             final String semester, final String defaultSemester) {
            this.index = index;
            this.subjects = subjects;
            this.semesters = semesters;
            this.semester = semester;
            this.defaultSemester = defaultSemester;
        }

        public SOCIndex getIndex() {
            return index;
        }

        public List<Subject> getSubjects() {
            return subjects;
        }

        public Semesters getSemesters() {
            return semesters;
        }

        public String getSemester() {
            return semester;
        }

        public String getDefaultSemester() {
            return defaultSemester;
        }
    }

    public static class ScheduleArgHolder {
        private final String level;
        private final String campus;
        private final String semester;

        public ScheduleArgHolder(final String level, final String campus, final String semester) {
            this.level = level;
            this.campus = campus;
            this.semester = semester;
        }

        public String getLevel() {
            return level;
        }

        public String getCampus() {
            return campus;
        }

        public String getSemester() {
            return semester;
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new SectionedScheduleAdapter(
            new SimpleSection<>("Subjects", new ArrayList<>()),
            new SimpleSection<>("Courses", new ArrayList<>()),
            R.layout.row_section_header,
            R.layout.row_title,
            R.id.title
        );

        // Restore filter
        if (savedInstanceState != null && filterEditText != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG, "");
            searching = savedInstanceState.getBoolean(SEARCHING_TAG);
            filterEditText.setText(mFilterString);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSearchUI();

        mAdapter.getPositionClicks()
            .flatMap(clickedItem -> {
                if (clickedItem instanceof Subject) {
                    return Observable.just(SOCCourses.createArgs(clickedItem.getDisplayTitle(), mCampus,
                            mSemester, mLevel, clickedItem.getCode()));
                } else if (clickedItem instanceof Course) {
                    // This is for when courses are loaded into the list by user-supplied filter
                    final Course course = (Course) clickedItem;
                    return Observable.just(SOCSections.createArgs(
                            course.getDisplayTitle(), mSemester, mSOCIndex.getCampusCode(),
                            course.getSubject(), course.getCourseNumber()
                    ));
                }

                return Observable.error(new IllegalStateException("SOC item must be Subject or Course"));
            })
            .subscribe(this::switchFragments, this::logError);
        getClasses();

    }
    public void getClasses() {
        // Load up schedule settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultSettings(sharedPref);
        final String prefLevel = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, SOCAPI.CODE_LEVEL_UNDERGRAD);
        final String prefCampus = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, SOCAPI.CODE_CAMPUS_NB);
        final String prefSemester = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null);

        final Bundle args = getArguments();
        mLevel = args.getString(ARG_LEVEL_TAG, prefLevel);
        mCampus = args.getString(ARG_CAMPUS_TAG, prefCampus);
        mSemester = args.getString(ARG_SEMESTER_TAG, prefSemester);

        // Register settings listener
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        setLoading(true);
        ScheduleArgHolder argHolder = new ScheduleArgHolder(mLevel, mCampus, mSemester);
        Observable.merge(
                Observable.just(argHolder),
                argHolderPublishSubject.asObservable()
        )
        .observeOn(Schedulers.io())
        .flatMap(scheduleArgHolder -> RutgersAPI.getSemesters()
        .flatMap(semesters -> {
            final String levelArg = scheduleArgHolder.getLevel();
            final String campusArg = scheduleArgHolder.getCampus();
            final String semesterArg = scheduleArgHolder.getSemester();

            int defaultIndex = semesters.getDefaultSemester();
            List<String> semesterStrings = semesters.getSemesters();

            if (semesterStrings.isEmpty()) {
                return Observable.error(new IllegalStateException("Semesters list is empty"));
            }

            if (defaultIndex < 0 || defaultIndex >= semesterStrings.size()) {
                defaultIndex = 0;
            }

            final String defaultSemester = semesterStrings.get(defaultIndex);

            final String semester = semesterArg != null && semesterStrings.contains(semesterArg)
                    ? semesterArg
                    : defaultSemester;

            return RutgersAPI.getSOCIndex(semester, campusArg, levelArg).flatMap(socIndex ->
                    SOCAPI.getSubjects(semester, campusArg, levelArg).map(subjects ->
                            new SubjectHolder(socIndex, subjects, semesters, semester, defaultSemester)
                    )
            );
        }))
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .retryWhen(this::logAndRetry)
        .subscribe(subjectHolder -> {
            reset();

            mSemester = subjectHolder.getSemester();
            mDefaultSemester = subjectHolder.getDefaultSemester();
            mSemesters = subjectHolder.getSemesters().getSemesters();
            mSOCIndex = subjectHolder.getIndex();

            mAdapter.addAllSubjects(subjectHolder.getSubjects());

            setScheduleTitle();
        }, this::handleErrorWithRetry);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_recycler_progress,
                CreateArgs.builder().toolbarRes(R.id.toolbar_search).build());

        setScheduleTitle();

        filterEditText = (EditText) v.findViewById(R.id.search_box);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

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

        if (mSemester != null) {
            pathParts.add(mSemester);
        }

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

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, SOCAPI.CODE_CAMPUS_NB);
            if (!mCampus.equals(temp)) {
                somethingChanged = true;
                mCampus = temp;
            }

            temp = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, SOCAPI.CODE_LEVEL_UNDERGRAD);
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
            argHolderPublishSubject.onNext(new ScheduleArgHolder(mLevel, mCampus, mSemester));
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
        if (userHome.equals(getString(R.string.campus_nb_tag))) campus = SOCAPI.CODE_CAMPUS_NB;
        else if (userHome.equals(getString(R.string.campus_nwk_tag))) campus = SOCAPI.CODE_CAMPUS_NWK;
        else if (userHome.equals(getString(R.string.campus_cam_tag))) campus = SOCAPI.CODE_CAMPUS_CAM;
        else campus = SOCAPI.CODE_CAMPUS_NB;

        // Pick default user-level code based on prefs (fall back to Undergrad)
        if (userLevel.equals(getString(R.string.role_undergrad_tag))) level = SOCAPI.CODE_LEVEL_UNDERGRAD;
        else if (userLevel.equals(getString(R.string.role_grad_tag))) level = SOCAPI.CODE_LEVEL_GRAD;
        else level = SOCAPI.CODE_LEVEL_UNDERGRAD;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PrefUtils.KEY_PREF_SOC_CAMPUS, campus);
        editor.putString(PrefUtils.KEY_PREF_SOC_LEVEL, level);
        editor.apply();
    }

    private void setScheduleTitle() {
        if (mSemester != null && mCampus != null && mLevel != null) {
            final String title = SOCAPI.translateSemester(mSemester) + " " + mCampus + " " + mLevel;
            getActivity().setTitle(title);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        mAdapter.clear();
    }
}