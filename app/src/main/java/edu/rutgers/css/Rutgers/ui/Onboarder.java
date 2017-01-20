package edu.rutgers.css.Rutgers.ui;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.ui.fragments.TutorialPageFragment;

public class Onboarder {
    private FragmentActivity ctx;
    LayoutInflater inflater;
    View tutorialView;
    ViewPager onboardPager;
    PagerAdapter mPagerAdapter;
    private int nextId = 0;
    private ArrayList<TutorialPageFragment> fragmentsList;
    public Onboarder(FragmentActivity context) {
        this.ctx = context;
        this.inflater = (LayoutInflater)this.ctx.getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
        this.tutorialView = inflater.inflate(R.layout.tutorial_layout, null);
        this.onboardPager = (ViewPager) this.tutorialView.findViewById(R.id.tutorial_view_pager);
        this.fragmentsList = new ArrayList<>();
    }

    public int insertNewPage(String src, String title, String body, int options) {
        TutorialPageFragment page = new TutorialPageFragment();
        page.setImage(src);
        page.stylizeTextElement(TutorialPageFragment.TutorialPageElement.TUTORIAL_PAGE_TITLE, title, TutorialPageFragment.ModifyValue.TEXT);
        page.stylizeTextElement(TutorialPageFragment.TutorialPageElement.TUTORIAL_PAGE_DESCR, body, TutorialPageFragment.ModifyValue.TEXT);
        this.fragmentsList.add(page);
        return this.fragmentsList.size();
    }

    public int insertNewFragment(TutorialPageFragment fragment, int options) {
        this.fragmentsList.add(fragment);
        return this.fragmentsList.size();
    }

    public int replacePageWithFragment(int pageId, TutorialPageFragment fragment, int options) {
        try {
            this.fragmentsList.set(pageId, fragment);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return pageId;
    }

    public boolean removePageWithId(int pageId) {
        try {
            this.fragmentsList.remove(pageId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean swapPages(int pageId1, int pageId2) {
        try {
            TutorialPageFragment f1 = this.fragmentsList.get(pageId1);
            TutorialPageFragment f2 = this.fragmentsList.get(pageId2);
            this.fragmentsList.set(pageId1, f2);
            this.fragmentsList.set(pageId2, f1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void cleanup() {

    }

    public void run() {
        this.mPagerAdapter = new OnboarderPagerAdapter(this.ctx.getSupportFragmentManager(), this.fragmentsList);
        this.onboardPager.setAdapter(this.mPagerAdapter);
    }

    public void editColorStyle(int pageId, TutorialPageFragment.TutorialPageElement textItem, String colorValue) {
        TutorialPageFragment f = this.fragmentsList.get(pageId);
        f.stylizeTextElement(textItem, colorValue, TutorialPageFragment.ModifyValue.COLOR);
    }

    public void editImage(int pageId, String src) {
        TutorialPageFragment f = this.fragmentsList.get(pageId);
        f.setImage(src);
    }

    public void editPageText(int pageId, TutorialPageFragment.TutorialPageElement textItem, String newText) {
        TutorialPageFragment f = this.fragmentsList.get(pageId);
        f.stylizeTextElement(textItem, newText, TutorialPageFragment.ModifyValue.TEXT);
    }

    private int getNextId(boolean incr) {
        int id = nextId;
        if (incr) nextId += 1;
        return id;
    }
}
