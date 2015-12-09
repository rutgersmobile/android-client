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
import edu.rutgers.css.Rutgers.channels.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.channels.soc.model.Semesters;
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

    private Bundle switchFood(List<String> pathParts) {
        String hall = LinkMaps.diningHalls.get(pathParts.get(0));
        return FoodHall.createArgs(hall);
    }

    private Bundle switchBus(Channel channel, List<String> pathParts) {
        String rsa = pathParts.remove(0);
        if (pathParts.size() == 0 && channel != null) {
            Bundle args = BusMain.createArgs(channel.getTitle(homeCampus));
            args.putInt(BusMain.ARG_START_TAG, LinkMaps.busPositions.get(rsa));
            return args;
        } else if (pathParts.size() > 0 && channel != null) {
            String stopOrRoute = pathParts.remove(0);
            String mode;
            switch (LinkMaps.busPositions.get(rsa)) {
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

        return null;
    }

    private Bundle switchDtable(Channel channel, List<String> pathParts) {
        try {
            JsonObject jsonObject;
            if (StringUtils.isNotBlank(channel.getApi())) {
                jsonObject = ApiRequest.api(channel.getApi(), TimeUnit.HOURS, JsonObject.class);
            } else {
                jsonObject = ApiRequest.json(channel.getUrl(), TimeUnit.HOURS, JsonObject.class);
            }

            DTableRoot root = new DTableRoot(jsonObject);
            for (String part : pathParts) {
                for (DTableElement child : root.getChildren()) {
                    if (child.getTitle()
                            .replaceAll("\\s+", "")
                            .toLowerCase()
                            .equals(part)) {
                        if (child instanceof DTableRoot) {
                            root = (DTableRoot) child;
                            break;
                        } else if (child instanceof DTableChannel) {
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
            }

            return DTable.createArgs(root.getTitle(), channel.getHandle(), root);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }

    private Bundle switchReader(Channel channel, List<String> pathParts) {
        return null;
    }

    private Bundle switchSOC(List<String> pathParts) {
        List<String> stripped = new ArrayList<>();
        String campusCode = null;
        String levelCode = null;
        String semesterCode = null;
        String subjectCode = null;
        String courseCode = null;

        if (defaultSemesterCode == null) {
            try {
                Semesters semesters = ScheduleAPI.getSemesters();
                defaultSemesterCode = semesters.getSemesters().get(semesters.getDefaultSemester());
            } catch (JsonSyntaxException | IOException e) {
                LOGE(TAG, e.getMessage());
                return null;
            }
        }

        final List<String> caps = new ArrayList<>();
        for (final String part : pathParts) {
            caps.add(part.toUpperCase());
        }

        for (String part : caps) {
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
                    stripped.add(part);
            }
        }

        if (stripped.size() > 0) {
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
            SOCIndex index = ScheduleAPI.getIndex(semesterCode, campusCode, levelCode);

            if (courseCode != null && subjectCode != null) {
                String title = index.getCourseByCode(subjectCode, courseCode).getTitle();
                return SOCSections.createArgs(title, semesterCode, campusCode, subjectCode, courseCode);
            }

            if (subjectCode != null) {
                String title = index.getSubjectByCode(subjectCode).getTitle();
                return SOCCourses.createArgs(title, campusCode, semesterCode, levelCode, subjectCode);
            }

            return SOCMain.createArgs(levelCode, campusCode, semesterCode);
        } catch (JsonSyntaxException | IOException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }

    private Bundle switchPlaces(List<String> pathParts) {
        return PlacesDisplay.createArgs(pathParts.get(0));
    }
}
