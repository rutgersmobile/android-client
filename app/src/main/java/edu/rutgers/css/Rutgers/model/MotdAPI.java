package edu.rutgers.css.Rutgers.model;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.rutgers.css.Rutgers.api.ApiRequest;

/**
 * Class for getting the message of the day from the Rutgers API
 */
public final class MotdAPI {
    private MotdAPI() {}

    private static final String RESOURCE = "motd.txt";
    public static final String TAG = "MotdAPI";

    private static Motd motd;
    private static boolean sSettingUp = false;

    private static void setup() throws JsonSyntaxException, IOException {
        if (sSettingUp) return;
        sSettingUp = true;
        motd = ApiRequest.api(RESOURCE, Motd.class);
        sSettingUp = false;
    }

    public static Motd getMotd() throws JsonSyntaxException, IOException {
        setup();
        return motd;
    }
}
