package edu.rutgers.css.Rutgers.link;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.soc.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusMain;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCCourses;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCSections;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Temporary class for synchronous loading of links
 */
public class LinkLoadTaskSync {

    private static final String TAG = "LinkLoadTaskSync";

    private String homeCampus;
    private boolean backstack;
    private Context context;

    public LinkLoadTaskSync(String homeCampus, boolean backstack, Context context) {
        this.homeCampus = homeCampus;
        this.backstack = backstack;
        this.context = context;
    }

    public Bundle execute(LinkLoadArgs linkArgs) {
        Channel channel = linkArgs.getChannel();
        String channelTag = channel.getView();
        List<String> pathParts = linkArgs.getPathParts();

        Bundle args = null;
        switch (channelTag) {
            case "food":
                args = switchFood(pathParts);
                break;
            case "bus":
                args = switchBus(channel, pathParts);
                break;
            case "reader":
                args = switchReader(channel, pathParts);
                break;
            case "soc":
                args = switchSOC(pathParts);
                break;
            case "places":
                args = switchPlaces(pathParts);
                break;
        }

        if (args != null) {
            args.putBoolean(ComponentFactory.ARG_BACKSTACK_TAG, backstack);
        }

        return args;
    }

    /**
     * @param pathParts /dining-hall-name
     * @return bundle for creating this fragment
     */
    public Bundle switchFood(List<String> pathParts) {
        if (pathParts.size() > 1) {
            return null;
        }

        String hall = LinkMaps.diningHalls.get(pathParts.get(0));
        if (hall == null) {
            return null;
        }

        return FoodHall.createArgs(hall);
    }

    /**
     * @param channel bus channel
     * @param pathParts /route
     *                  /stop
     *                  /all
     *                  /route/route-name
     *                  /stop/stop-name
     * @return bundle to start this fragment
     */
    public Bundle switchBus(Channel channel, List<String> pathParts) {
        if (pathParts.size() > 2) {
            return null;
        }

        // get the type of object to display: route, stop, or all
        String rsa = pathParts.remove(0);
        Integer startTab = LinkMaps.busPositions.get(rsa);
        if (startTab == null) {
            return null;
        }

        if (pathParts.size() == 0) {
            // no route or stop name given
            return BusMain.createArgs(channel.getTitle(homeCampus), startTab);
        }

        // link to the route or stop given
        String stopOrRoute = pathParts.remove(0);
        String mode;
        switch (startTab) {
            case 1:
                mode = BusDisplay.STOP_MODE;
                break;
            default:
                mode = BusDisplay.ROUTE_MODE;
                break;
        }

        return BusDisplay.createLinkArgs(mode,
                NextbusAPI.AGENCY_NB, stopOrRoute);
    }
    /**
     * This doesn't really have a place to link to yet. Channels with reader are really only
     * launched from a dtable.
     */
    public Bundle switchReader(Channel channel, List<String> pathParts) {
        return null;
    }

    public Bundle switchPlaces(List<String> pathParts) {
        return PlacesDisplay.createArgs(pathParts.get(0));
    }

    /**
     * Almost every field is optional for SoC. Subject and course codes are
     * indistinguishable and must always be in order. Semester should be before these two as well.
     * @param pathParts /nb/u/92015/010/272
     *                  /u/92015/nb/010/272
     *                  /010/272
     *                  /010
     *                  /92015
     *                  /92015/010/272
     * @return bundle to start this fragment
     */
    public Bundle switchSOC(List<String> pathParts) {
        List<String> stripped = new ArrayList<>();

        String campusCode = PrefUtils.getHomeCampus(context);
        String levelCode = null;
        String semesterCode = null;
        String subjectCode = null;
        String courseCode = null;

        // all codes are capitalized
        final List<String> caps = new ArrayList<>();
        for (final String part : pathParts) {
            caps.add(part.toUpperCase());
        }

        // take out everything that isn't a number
        for (final String part : caps) {
            switch (part.toUpperCase()) {
                case ScheduleAPI.CODE_CAMPUS_NB:
                case ScheduleAPI.CODE_CAMPUS_CAM:
                case ScheduleAPI.CODE_CAMPUS_NWK:
                case ScheduleAPI.CODE_CAMPUS_ONLINE:
                    campusCode = part;
                    break;
                case ScheduleAPI.CODE_LEVEL_UNDERGRAD:
                case ScheduleAPI.CODE_LEVEL_GRAD:
                    levelCode = part;
                    break;
                default:
                    try {
                        Integer.parseInt(part);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                    stripped.add(part);
            }
        }

        if (stripped.size() > 0) {
            // It's tough to tell when we're on the semester, so this
            // is our best guess. Keeping the order helps a lot
            if (stripped.get(0).length() > 3) {
                semesterCode = stripped.remove(0);
            }

            if (stripped.size() > 0) {
                subjectCode = stripped.remove(0);
                if (stripped.size() > 0) {
                    courseCode = stripped.remove(0);
                }
            }
        }

        if (stripped.size() > 0) {
            return null;
        }

        if (semesterCode == null) {
            return null;
        }

        if (courseCode != null && subjectCode != null) {
            return SOCSections.createArgs(semesterCode, campusCode, subjectCode, courseCode);
        }

        if (levelCode == null) {
            return null;
        }

        if (subjectCode != null) {
            return SOCCourses.createArgs(campusCode, semesterCode, levelCode, subjectCode);
        }

        return SOCMain.createArgs(levelCode, campusCode, semesterCode);
    }
}
