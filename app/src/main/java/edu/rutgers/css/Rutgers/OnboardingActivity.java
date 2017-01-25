package edu.rutgers.css.Rutgers;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.ui.OnboarderPagerAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.TutorialPageFragment;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

public class OnboardingActivity extends AppCompatActivity {
    ViewPager pager;
    Button finish, nextFragment;
    RelativeLayout parent;
    private static final String TAG = "ONBOARDACT";
    ArrayList<Fragment> tutorialFragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        this.pager = (ViewPager)findViewById(R.id.onboarding_view_pager);
        this.tutorialFragments = new ArrayList<Fragment>();
        this.finish = (Button)findViewById(R.id.onboarding_done);
        this.nextFragment = (Button)findViewById(R.id.tutorial_to_next_fragment);
        this.parent = (RelativeLayout)findViewById(R.id.activity_onboarding_parent_layout);
        this.finish.setOnClickListener(view -> {
            PrefUtils.markFirstLaunch(getApplicationContext());
            finish();
        });
        this.nextFragment.setOnClickListener(view -> loadNextItem());
        addPreloadedFragment(R.drawable.sample_image_1, "Report 1022", "We have encountered a mysterious object while out on observation. It appears to be solid in nature and yet our computers are unable to pick up its volumetrics. Approaching for inspection.");
        addPreloadedFragment(R.drawable.sample_image_2, "Report 1023", "Lt. Johnson approached the object at 2100 hours and \"fell\" into it. We have made repeated attempts to contact him but have been unable to reach his comms. Presumed dead.");
        loadAdapter();
    }

    public void loadAdapter() {
        OnboarderPagerAdapter adapter = new OnboarderPagerAdapter(getSupportFragmentManager(), this.tutorialFragments);
        this.pager.setAdapter(adapter);
    }

    public void addCustomFragment(Fragment fragment) {
        this.tutorialFragments.add(fragment);
    }

    public void setMainBackground(String color) {
        this.parent.setBackgroundColor(Color.parseColor(color));
    }

    public void addPreloadedFragment(int imgsrc, String title, String description) {
        TutorialPageFragment tutorialFragment = new TutorialPageFragment();
        tutorialFragment.presetImageDisplayed(imgsrc);
        tutorialFragment.stylizeElement(TutorialPageFragment.TutorialPageElement.TUTORIAL_PAGE_TITLE, title, TutorialPageFragment.ModifyValue.TEXT);
        tutorialFragment.stylizeElement(TutorialPageFragment.TutorialPageElement.TUTORIAL_PAGE_DESCR, description, TutorialPageFragment.ModifyValue.TEXT);
        this.tutorialFragments.add(tutorialFragment);
    }

    public void loadNextItem() {
        int pos = this.pager.getCurrentItem();
        if (pos + 1 >= this.pager.getAdapter().getCount()) {
            PrefUtils.markFirstLaunch(getApplicationContext());
            finish();
        } else {
            this.pager.setCurrentItem(pos+1);
        }
    }
}
