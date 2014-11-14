package edu.rutgers.css.Rutgers.channels.bus.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Nextbus agency configuration.
 */
public class AgencyConfig {
    private HashMap<String, Route> routes;
    private HashMap<String, Stop> stops;
    private HashMap<String, StopGroup> stopsByTitle;
    private List<StopStub> sortedStops;
    private List<RouteStub> sortedRoutes;

    public AgencyConfig(JSONObject configJson) throws JSONException, JsonSyntaxException {
        Gson gson = new Gson();

        // Deserialize the routes table
        this.routes = new HashMap<>();
        JSONObject routesJson = configJson.getJSONObject("routes");
        for(Iterator<String> routeTags = routesJson.keys(); routeTags.hasNext();) {
            String routeTag = routeTags.next();
            JSONObject routeJson = routesJson.getJSONObject(routeTag);
            Route route = gson.fromJson(routeJson.toString(), Route.class);
            route.setTitle(routeTag);
            this.routes.put(routeTag, route);
        }

        // Deserialize the stops table
        this.stops = new HashMap<>();
        JSONObject stopsJson = configJson.getJSONObject("stops");
        for(Iterator<String> stopTags = stopsJson.keys(); stopTags.hasNext();) {
            String stopTag = stopTags.next();
            JSONObject stopJson = stopsJson.getJSONObject(stopTag);
            Stop stop = gson.fromJson(stopJson.toString(), Stop.class);
            stop.setTitle(stopTag);
            this.stops.put(stopTag, stop);
        }

        // Deserialize the stopsByTitle table
        this.stopsByTitle = new HashMap<>();
        JSONObject stopGroupsJson = configJson.getJSONObject("stopsByTitle");
        for(Iterator<String> stopGroupTags = stopGroupsJson.keys(); stopGroupTags.hasNext();) {
            String stopGroupTag = stopGroupTags.next();
            JSONObject stopGroupJson = stopGroupsJson.getJSONObject(stopGroupTag);
            StopGroup stopGroup = gson.fromJson(stopGroupJson.toString(), StopGroup.class);
            stopGroup.setTitle(stopGroupTag);
            this.stopsByTitle.put(stopGroupTag, stopGroup);
        }

        // Deserialize the sorted stop stubs
        JSONArray sortedStopsJson = configJson.getJSONArray("sortedStops");
        StopStub stopStubs[] = gson.fromJson(sortedStopsJson.toString(), StopStub[].class);
        this.sortedStops = Arrays.asList(stopStubs);

        // Deserialize the sorted route stubs
        JSONArray sortedRoutesJson = configJson.getJSONArray("sortedRoutes");
        RouteStub routeStubs[] = gson.fromJson(sortedRoutesJson.toString(), RouteStub[].class);
        this.sortedRoutes = Arrays.asList(routeStubs);
    }

    public HashMap<String, Route> getRoutes() {
        return routes;
    }

    public HashMap<String, Stop> getStops() {
        return stops;
    }

    public HashMap<String, StopGroup> getStopsByTitle() {
        return stopsByTitle;
    }

    public List<StopStub> getSortedStops() {
        return sortedStops;
    }

    public List<RouteStub> getSortedRoutes() {
        return sortedRoutes;
    }
}
