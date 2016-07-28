package edu.rutgers.css.Rutgers.link;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.transition.ActionBarTransition;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.channels.dtable.model.VarTitle;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;
import edu.rutgers.css.Rutgers.model.Channel;

/**
 * Deep linking unit tests
 */
@RunWith(AndroidJUnit4.class)
public class LinkLoadTaskTest {

    private LinkLoadTaskSync task;
    private LinkLoadTask asyncTask;

    @Before
    public void setUp() {
        task = new LinkLoadTaskSync("New Brunswick", "NB", "U", "92015");
    }

    @Test
    public void switchFood() {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add("brower");

        final Bundle expected = new Bundle();
        expected.putString("component", "foodhall");
        expected.putString("title", "Brower Commons");

        final Bundle bundle = task.switchFood(pathParts);
        Assert.assertTrue("should have handle and location", equalBundles(expected, bundle));
    }

    @Test
    public void switchFoodBroken() {
        final List<String> invalid = new ArrayList<>();
        invalid.add("nonsense");

        final Bundle invalidBundle = task.switchFood(invalid);
        Assert.assertNull("must specify dining hall", invalidBundle);

        final List<String> tooLong = new ArrayList<>();
        tooLong.add("brower");
        tooLong.add("nonsense");

        final Bundle tooLongBundle = task.switchFood(tooLong);
        Assert.assertNull("shouldn't allow more than one argument", tooLongBundle);
    }

    @Test
    public void switchBus() {
        final Channel channel = new Channel(new VarTitle("Bus"), null, null, null, null, null, false);
        final List<String> routeParts = new ArrayList<>();
        routeParts.add("route");

        final Bundle routeExpected = new Bundle();
        routeExpected.putString("component", "bus");
        routeExpected.putString("title", "Bus");
        routeExpected.putInt("startTag", 0);

        final Bundle routeBundle = task.switchBus(channel, routeParts);

        Assert.assertTrue("should switch to bus routes", equalBundles(routeExpected, routeBundle));

        final List<String> stopParts = new ArrayList<>();
        stopParts.add("stop");

        final Bundle stopExpected = new Bundle();
        stopExpected.putString("component", "bus");
        stopExpected.putString("title", "Bus");
        stopExpected.putInt("startTag", 1);

        final Bundle stopBundle = task.switchBus(channel, stopParts);

        Assert.assertTrue("should switch to bus stop", equalBundles(stopExpected, stopBundle));

        final List<String> displayStopParts = new ArrayList<>();
        displayStopParts.add("route");
        displayStopParts.add("a");

        final Bundle displayStopExpected = new Bundle();
        displayStopExpected.putString("component", "busdisplay");
        displayStopExpected.putString("agency", "nb");
        displayStopExpected.putString("mode", "route");
        displayStopExpected.putString("tag", "a");

        final Bundle displayStop = task.switchBus(channel, displayStopParts);

        Assert.assertTrue("should switch to stop display", equalBundles(displayStopExpected, displayStop));
    }

    @Test
    public void switchBusBroken() {
        final Channel channel = new Channel(new VarTitle("Bus"), null, null, null, null, null, false);
        final List<String> routeParts = new ArrayList<>();
        routeParts.add("nonsense");

        final Bundle badRSA = task.switchBus(channel, routeParts);
        Assert.assertNull("should not switch on when not route/stop/all", badRSA);

        final List<String> badTagParts = new ArrayList<>();
        badTagParts.add("route");
        badTagParts.add("nonsense");

        final Bundle badTag = task.switchBus(channel, badTagParts);
        Assert.assertNull("should not switch on invalid route", badTag);

        final List<String> tooLongParts = new ArrayList<>();
        tooLongParts.add("stop");
        tooLongParts.add("a");
        tooLongParts.add("a");

        final Bundle tooLong = task.switchBus(channel, tooLongParts);
        Assert.assertNull("should not switch with too many arguments", tooLong);
    }

    @Test
    public void switchDtable() {
        final Channel channel = new Channel(null, "knights", null, "athleticsfaq.txt", null, null, false);
        final List<String> rootParts = new ArrayList<>();
        rootParts.add("athleticsschedules");

        final Bundle rootBundle = asyncTask.switchDtable(channel, rootParts);
        String component = rootBundle.getString("component");
        String title = rootBundle.getString("title");
        String handle = rootBundle.getString("handle");
        Object root = rootBundle.getSerializable(Config.PACKAGE_NAME + ".dtable.data");

        Assert.assertEquals("should have correct component", "dtable", component);
        Assert.assertEquals("should have correct title", "Athletics Schedules", title);
        Assert.assertEquals("should have correct handle", "knights", handle);
        Assert.assertTrue("should have a dtable root", root instanceof DTableRoot);

        rootParts.add("basketball-men");

        final Bundle expectedChannelBundle = new Bundle();
        expectedChannelBundle.putString("component", "Reader");
        expectedChannelBundle.putString("url", "https://rumobile.rutgers.edu/1/sports/m_basketball.xml");
        expectedChannelBundle.putString("title", "Basketball - Men");

        final Bundle channelBundle = asyncTask.switchDtable(channel, rootParts);
        Assert.assertTrue("should switch to rss reader channel", equalBundles(expectedChannelBundle, channelBundle));
    }

    @Test
    public void switchDtableBroken() {
        final Channel channel = new Channel(null, "knights", null, "athleticsfaq.txt", null, null, false);
        final List<String> singleBadPath = new ArrayList<>();
        singleBadPath.add("nonsense");

        final Bundle singleBad = asyncTask.switchDtable(channel, singleBadPath);
        Assert.assertNull("should not switch on bad path", singleBad);

        final List<String> lastBadPath = new ArrayList<>();
        lastBadPath.add("athleticsschedules");
        lastBadPath.add("nonsense");

        final Bundle lastBad = asyncTask.switchDtable(channel, lastBadPath);
        Assert.assertNull("should not switch on bad path", lastBad);

        final List<String> extraPath = new ArrayList<>();
        extraPath.add("athleticsschedules");
        extraPath.add("basketball-men");
        extraPath.add("nonsense");

        final Bundle extra = asyncTask.switchDtable(channel, extraPath);
        Assert.assertNull("should not switch on bad path", extra);
    }

    @Test
    public void switchSOC() {
        final List<String> nbParts = new ArrayList<>();
        nbParts.add("nk");

        final Bundle expectedNB = new Bundle();
        expectedNB.putString("component", "soc");
        expectedNB.putString("level", "U");
        expectedNB.putString("campus", "NK");
        expectedNB.putString("semester", "92015");

        final Bundle nbBundle = task.switchSOC(nbParts);
        Assert.assertTrue("should go to SoC main with correct level/campus/semester", equalBundles(expectedNB, nbBundle));

        final List<String> gParts = new ArrayList<>();
        gParts.add("g");

        final Bundle gExpected = new Bundle();
        gExpected.putString("component", "soc");
        gExpected.putString("level", "G");
        gExpected.putString("campus", "NB");
        gExpected.putString("semester", "92015");

        final Bundle gBundle = task.switchSOC(gParts);
        Assert.assertTrue("should go to SoC main with correct level/campus/semester", equalBundles(gExpected, gBundle));

        gParts.add("nk");
        gExpected.remove("campus");
        gExpected.putString("campus", "NK");

        final Bundle twoParts = task.switchSOC(gParts);
        Assert.assertTrue("should go to SoC main with correct level/campus/semester", equalBundles(gExpected, twoParts));

        final List<String> sectionParts = new ArrayList<>();
        sectionParts.add("010");

        final Bundle expectedSections = new Bundle();
        expectedSections.putString("component", "soccourses");
        expectedSections.putString("title", "ACCOUNTING");
        expectedSections.putString("campus", "NB");
        expectedSections.putString("semester", "92015");
        expectedSections.putString("level", "U");
        expectedSections.putString("subject", "010");

        final Bundle sectionBundle = task.switchSOC(sectionParts);
        Assert.assertTrue("should switch to SoC courses to correct subject", equalBundles(expectedSections, sectionBundle));

        final List<String> courseParts = new ArrayList<>();
        courseParts.add("nk");
        courseParts.add("010");
        courseParts.add("304");

        final Bundle expectedCourses = new Bundle();
        expectedCourses.putString("component", "socsections");
        expectedCourses.putString("title", "Cost Accounting");
        expectedCourses.putString("semester", "92015");
        expectedCourses.putString("campus", "NK");
        expectedCourses.putString("subject", "010");
        expectedCourses.putString("course", "304");

        final Bundle coursesBundle = task.switchSOC(courseParts);
        Assert.assertTrue("should switch to SoC sections to correct course", equalBundles(expectedCourses, coursesBundle));
    }

    @Test
    public void switchSOCBroken() {
        final List<String> badPath = new ArrayList<>();
        badPath.add("nonsense");
        final Bundle badBundle = task.switchSOC(badPath);
        Assert.assertNull("should not switch on bad path", badBundle);

        final List<String> badSemester = new ArrayList<>();
        badSemester.add("9999");
        final Bundle badSemesterBundle = task.switchSOC(badSemester);
        Assert.assertNull("should not switch on bad semester", badSemesterBundle);

        final List<String> badSubjectPath = new ArrayList<>();
        badSubjectPath.add("999");
        final Bundle badSubjectBundle = task.switchSOC(badSubjectPath);
        Assert.assertNull("should not switch on bad subject", badSubjectBundle);

        final List<String> badCoursePath = new ArrayList<>();
        badCoursePath.add("010");
        badCoursePath.add("999");
        final Bundle badCourseBundle = task.switchSOC(badCoursePath);
        Assert.assertNull("should not switch on bad course", badCourseBundle);
    }

    private static boolean equalBundles(final Bundle first, final Bundle second) {
        if (first == null || second == null) {
            return false;
        }

        if (first.size() != second.size()) {
            return false;
        }

        for (final String key : first.keySet()) {
            Object firstVal = first.get(key);
            Object secondVal = second.get(key);
            if (firstVal == null || secondVal == null) {
                return false;
            }

            if (firstVal instanceof Bundle && secondVal instanceof Bundle
                    && !equalBundles((Bundle) firstVal, (Bundle) secondVal)) {
                return false;
            }

            if (!firstVal.equals(secondVal)) {
                return false;
            }
        }

        return true;
    }
}