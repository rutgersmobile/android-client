package edu.rutgers.css.Rutgers.api.bus.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.api.bus.model.AgencyConfig;
import edu.rutgers.css.Rutgers.api.bus.model.route.Route;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.Stop;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;

/**
 * For special deserialization of an AgencyConfig to be used with Gson
 */
public class AgencyConfigDeserializer implements JsonDeserializer<AgencyConfig> {

    private String agency;

    public AgencyConfigDeserializer(String agency) {
        this.agency = agency;
    }

    @Override
    public AgencyConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Gson gson = new Gson();

        // Deserialize the routes table
        HashMap<String, Route> routes = new HashMap<>();
        JsonObject routesJson = jsonObject.getAsJsonObject("routes");
        for (Map.Entry<String, JsonElement> e : routesJson.entrySet()) {
            String routeTag = e.getKey();
            JsonObject routeJson = e.getValue().getAsJsonObject();
            Route route = gson.fromJson(routeJson, Route.class);
            route.setTitle(routeTag);
            route.setAgencyTag(agency);
            routes.put(routeTag, route);
        }

        // Deserialize the stops table
        HashMap<String, Stop> stops = new HashMap<>();
        JsonObject stopsJson = jsonObject.getAsJsonObject("stops");
        for (Map.Entry<String, JsonElement> e : stopsJson.entrySet()) {
            String stopTag = e.getKey();
            JsonObject stopJson = e.getValue().getAsJsonObject();
            Stop stop = gson.fromJson(stopJson, Stop.class);
            stop.setTitle(stopTag);
            stop.setAgencyTag(agency);
            stops.put(stopTag, stop);
        }

        // Deserialize the stopsByTitle table
        HashMap<String, StopGroup> stopsByTitle = new HashMap<>();
        JsonObject stopGroupsJson = jsonObject.getAsJsonObject("stopsByTitle");
        for (Map.Entry<String, JsonElement> e : stopGroupsJson.entrySet()) {
            String stopGroupTag = e.getKey();
            JsonObject stopGroupJson = e.getValue().getAsJsonObject();
            StopGroup stopGroup = gson.fromJson(stopGroupJson, StopGroup.class);
            stopGroup.setTitle(stopGroupTag);
            stopGroup.setAgencyTag(agency);
            stopsByTitle.put(stopGroupTag, stopGroup);
        }

        // Deserialize the sorted stop stubs
        JsonArray sortedStopsJson = jsonObject.getAsJsonArray("sortedStops");
        StopStub stopStubs[] = gson.fromJson(sortedStopsJson, StopStub[].class);
        List<StopStub> sortedStops = Arrays.asList(stopStubs);
        for (StopStub stopStub: sortedStops) stopStub.setAgencyTag(agency);

        // Deserialize the sorted route stubs
        JsonArray sortedRoutesJson = jsonObject.getAsJsonArray("sortedRoutes");
        RouteStub routeStubs[] = gson.fromJson(sortedRoutesJson, RouteStub[].class);
        List<RouteStub> sortedRoutes = Arrays.asList(routeStubs);
        for (RouteStub routeStub: sortedRoutes) routeStub.setAgencyTag(agency);

        return new AgencyConfig(routes, stops, stopsByTitle, sortedStops, sortedRoutes, agency);
    }
}
