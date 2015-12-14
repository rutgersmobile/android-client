package edu.rutgers.css.Rutgers.link;

import android.os.Bundle;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;

/**
 * Deep linking unit tests
 */
public class LinkLoadTaskTest {

    private LinkLoadTask task;

    @Before
    public void setUp() throws Exception {
        task = new LinkLoadTask("New Brunswick", "NB", "U", null);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void switchFood() {
        final String hall = "brower";

        final List<String> pathParts = new ArrayList<>();
        pathParts.add(hall);

        final Bundle expected = new Bundle();
        expected.putString(ComponentFactory.ARG_COMPONENT_TAG, FoodHall.HANDLE);
        expected.putString(FoodHall.ARG_TITLE_TAG, hall);

        final Bundle bundle = task.switchFood(pathParts);
        Assert.assertTrue("should have handle and location", equalBundles(expected, bundle));
    }

    private static boolean equalBundles(final Bundle first, final Bundle second) {
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