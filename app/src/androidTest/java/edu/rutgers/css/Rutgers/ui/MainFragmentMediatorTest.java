package edu.rutgers.css.Rutgers.ui;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test of the fragment mediator used by the main activity.
 */
@RunWith(AndroidJUnit4.class)
public class MainFragmentMediatorTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = activityRule.getActivity();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSwitchFragments() throws Exception {

    }
}