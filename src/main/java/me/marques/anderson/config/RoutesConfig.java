package me.marques.anderson.config;

public class RoutesConfig {

    private static final String TOGGLES_PATH = "/toggles";
    private static final String TOGGLE_PATH = "/toggles/:name";
    private static final String TOGGLES_VALUES_PATH = "/services/:identifier/versions/:version/toggle-values";
    private static final String PING_PATH = "/ping";
    private static final String OAUTH_TOKEN_PATH = "/oauth/token";

    public String getTogglePath() {
        return TOGGLE_PATH;
    }

    public String getTogglesPath() {
        return TOGGLES_PATH;
    }

    public String getTogglesValuesPath() {
        return TOGGLES_VALUES_PATH;
    }

    public String getPingPath() {
        return PING_PATH;
    }

    public String getOauthTokenPath() {
        return OAUTH_TOKEN_PATH;
    }
}
