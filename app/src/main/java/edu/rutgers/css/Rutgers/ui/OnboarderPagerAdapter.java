package edu.rutgers.css.Rutgers.ui;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import edu.rutgers.css.Rutgers.ui.fragments.TutorialPageFragment;

/**
 * Created by rz187 on 1/20/17.
 */

public class OnboarderPagerAdapter extends FragmentPagerAdapter {
    public static int pos = 0;
    private List<TutorialPageFragment> tutorialFragments;

    public OnboarderPagerAdapter(FragmentManager fm, List<TutorialPageFragment> tutorialFragments) {
        super(fm);
        this.tutorialFragments = tutorialFragments;
    }

    public TutorialPageFragment getItem(int p) {
        return this.tutorialFragments.get(p);
    }

    public int getCount() {
        return this.tutorialFragments.size();
    }

    public CharSequence getPageTitle(int p) {
        TutorialPageFragment tpf = getItem(p);
        String[] data = tpf.getPageElementData(TutorialPageFragment.TutorialPageElement.TUTORIAL_PAGE_TITLE);
        return data[0];
    }
}
