package edu.rutgers.css.Rutgers.link;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusDisplay;
import edu.rutgers.css.Rutgers.channels.bus.fragments.BusMain;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableChannel;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableElement;
import edu.rutgers.css.Rutgers.channels.dtable.model.DTableRoot;
import edu.rutgers.css.Rutgers.channels.food.fragments.FoodHall;
import edu.rutgers.css.Rutgers.channels.places.fragments.PlacesDisplay;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCCourses;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCMain;
import edu.rutgers.css.Rutgers.channels.soc.fragments.SOCSections;
import edu.rutgers.css.Rutgers.channels.soc.model.Course;
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
import edu.rutgers.css.Rutgers.channels.soc.model.Subject;
import edu.rutgers.css.Rutgers.model.Channel;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Perform the initial load and post to an event bus
 */
public class LinkLoadTask extends AsyncTask<LinkLoadArgs, Void, Bundle> {
    private final String homeCampus;
    private final String defaultCampusCode;
    private final String defaultLevelCode;
    private String defaultSemesterCode;

    private static final String TAG = "InitLoadAsyncTask";

    public LinkLoadTask(final String homeCampus,
                        final String defaultCampusCode,
                        final String defaultLevelCode,
                        final String defaultSemesterCode) {
        super();
        this.homeCampus = homeCampus;
        this.defaultCampusCode = defaultCampusCode;
        this.defaultLevelCode = defaultLevelCode;
        this.defaultSemesterCode = defaultSemesterCode;
    }

    @Override
    protected Bundle doInBackground(LinkLoadArgs... args) {
        if (args.length != 1) {
            return null;
        }

        LinkLoadArgs firstArgs = args[0];
        Channel channel = firstArgs.getChannel();
        String channelTag = channel.getView();
        List<String> pathParts = firstArgs.getPathParts();

        switch (channelTag) {
            case "food":
                return switchFood(pathParts);
            case "bus":
                return switchBus(channel, pathParts);
            case "dtable":
                return switchDtable(channel, pathParts);
            case "reader":
                return switchReader(channel, pathParts);
            case "soc":
                return switchSOC(pathParts);
            case "places":
                return switchPlaces(pathParts);
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute(Bundle args) {
        if (args != null) {
            LinkBus.getInstance().post(args);
        }
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
     * @param channel dtable
     * @param pathParts names of dtable elements
     * @return bundle to start this dtable
     */
    public Bundle switchDtable(Channel channel, List<String> pathParts) {
        try {
            // JSON representing the dtable
            JsonObject jsonObject;
            if (StringUtils.isNotBlank(channel.getApi())) {
                jsonObject = ApiRequest.api(channel.getApi(), TimeUnit.HOURS, JsonObject.class);
            } else {
                jsonObject = ApiRequest.json(channel.getUrl(), TimeUnit.HOURS, JsonObject.class);
            }

            DTableRoot root = new DTableRoot(jsonObject, null);
            for (int i = 0; i < pathParts.size(); i++) {
                final String part = pathParts.get(i);
                boolean found = false;
                for (final DTableElement child : root.getChildren()) {
                    // no spaces or capital letters in input
                    if (child.getTitle()
                            .replaceAll("\\s+", "")
                            .toLowerCase()
                            .equals(part)) {
                        if (child instanceof DTableRoot) {
                            // look for the next element in this root's children
                            root = (DTableRoot) child;
                            found = true;
                            break;
                        } else if (child instanceof DTableChannel) {
                            if (i != pathParts.size() - 1) {
                                return null;
                            }

                            DTableChannel dTableChannel = (DTableChannel) child;
                            Bundle newArgs = new Bundle();
                            // Must have view and title set to launch a channel
                            newArgs.putString(ComponentFactory.ARG_COMPONENT_TAG, dTableChannel.getView());
                            newArgs.putString(ComponentFactory.ARG_TITLE_TAG, dTableChannel.getChannelTitle(homeCampus));

                            // Add optional fields to the arg bundle
                            if (dTableChannel.getUrl() != null) {
                                newArgs.putString(ComponentFactory.ARG_URL_TAG, dTableChannel.getUrl());
                            }

                            if (dTableChannel.getData() != null) {
                                newArgs.putString(ComponentFactory.ARG_DATA_TAG, dTableChannel.getData());
                            }

                            if (dTableChannel.getCount() > 0) {
                                newArgs.putInt(ComponentFactory.ARG_COUNT_TAG, dTableChannel.getCount());
                            }

                            return newArgs;
                        }
                    }
                }
                if (!found) {
                    return null;
                }
            }

            return DTable.createArgs(root.getTitle(), channel.getHandle(), null, root);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * This doesn't really have a place to link to yet. Channels with reader are really only
     * launched from a dtable.
     */
    public Bundle switchReader(Channel channel, List<String> pathParts) {
        return null;
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

        String campusCode = null;
        String levelCode = null;
        String semesterCode = null;
        String subjectCode = null;
        String courseCode = null;

        // Default semester can be determined from the API
        if (defaultSemesterCode == null) {
            try {
                Semesters semesters = ScheduleAPI.getSemesters();
                defaultSemesterCode = semesters.getSemesters().get(semesters.getDefaultSemester());
            } catch (JsonSyntaxException | IOException e) {
                LOGE(TAG, e.getMessage());
                return null;
            }
        }

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

            // Subject and course codes are in order if there at all
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

        // Set defaults if we haven't gotten anything for semester/campus/level

        if (campusCode == null) {
            campusCode = defaultCampusCode;
        }

        if (semesterCode == null) {
            semesterCode = defaultSemesterCode;
        }

        if (levelCode == null) {
            levelCode = defaultLevelCode;
        }

        try {
            // We need the index to get titles of the course / subject we're linking to
            SOCIndex index = ScheduleAPI.getIndex(semesterCode, campusCode, levelCode);
            if (index == null) {
                return null;
            }

            if (courseCode != null && subjectCode != null) {
                Course course = index.getCourseByCode(subjectCode, courseCode);
                if (course == null) {
                    return null;
                }
                return SOCSections.createArgs(course.getTitle(), semesterCode, campusCode, subjectCode, courseCode);
            }

            if (subjectCode != null) {
                Subject subject = index.getSubjectByCode(subjectCode);
                if (subject == null) {
                    return null;
                }
                return SOCCourses.createArgs(subject.getTitle(), campusCode, semesterCode, levelCode, subjectCode);
            }

            return SOCMain.createArgs(levelCode, campusCode, semesterCode);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }

    public Bundle switchPlaces(List<String> pathParts) {
        return PlacesDisplay.createArgs(pathParts.get(0));
    }
}
