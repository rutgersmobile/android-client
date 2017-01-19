package edu.rutgers.css.Rutgers.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

import java.util.HashMap;
import java.util.LinkedHashMap;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.ui.fragments.TutorialPageFragment;

/**
 * Created by rz187 on 1/19/17.
 */

public class Onboarder {
    private Context ctx;
    LayoutInflater inflater;
    View tutorialView;
    public static enum PageElement {
        TUTORIAL_PAGE_TITLE,
        TUTORIAL_PAGE_IMAGE,
        TUTORIAL_PAGE_DESCRIPTION,
        TUTORiAL_PAGE_BUTTON
    }

    private LinkedHashMap<Integer, Fragment> pages;
    public Onboarder(Context context) {
        this.ctx = context;
        this.inflater = (LayoutInflater)this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tutorialView = inflater.inflate(R.layout.tutorial_layout, null);
        this.pages = new LinkedHashMap<>();
    }

    public int insertNewPage(String src, String title, String body, int options) {
        TutorialPageFragment page = new TutorialPageFragment();

    }

    public int insertNewFragment(Fragment fragment, int options) {

    }

    public int replacePageWithFragment(int pageId, Fragment fragment, int options) {

    }

    public int removePageWithId(int pageId) {

    }

    public boolean swapPages(int pageId1, int pageId2) {

    }

    public void cleanup() {

    }

    public void run() {

    }

    public void editColorStyle(int pageId, PageElement textItem, String colorValue) {

    }

    public void editImage(int pageId, String src) {

    }

    public void editPageText(int pageId, PageElement textItem, String newText) {

    }
}
