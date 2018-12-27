package me.marques.anderson.config;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentValues {

    public static final String WEBAPP_PORT = "WEBAPP_PORT";

    public static final String RABBITMQ_USER = "RABBITMQ_USER";
    public static final String RABBITMQ_PASS = "RABBITMQ_PASS";
    public static final String RABBITMQ_HOST = "RABBITMQ_HOST";
    public static final String RABBITMQ_PORT = "RABBITMQ_PORT";

    public static final String MONGO_HOST = "MONGO_HOST";
    public static final String MONGO_PORT = "MONGO_PORT";

    public static final String ADMIN_PASS = "ADMIN_PASS";

    public static final String JWT_TOKEN_SECRET = "JWT_TOKEN_SECRET";
    public static final String JWT_TOKEN_AUDIENCE = "JWT_TOKEN_AUDIENCE";
    public static final String JWT_TOKEN_ISSUER = "JWT_TOKEN_ISSUER";
    public static final String JWT_TOKEN_LIFETIME_IN_SECONDS = "JWT_TOKEN_LIFETIME_IN_SECONDS";

    private static final String DEFAULT_WEBAPP_PORT = "8080";
    private static final String DEFAULT_RABBITMQ_USER = "guest";
    private static final String DEFAULT_RABBITMQ_PASS = "guest";
    private static final String DEFAULT_RABBITMQ_HOST = "localhost";
    private static final String DEFAULT_RABBITMQ_PORT = "5672";

    private static final String DEFAULT_MONGO_HOST = "localhost";
    private static final String DEFAULT_MONGO_PORT = "27017";

    private static final String DEFAULT_ADMIN_PASS = "admin";

    private static final String DEFAULT_JWT_TOKEN_SECRET = "WzqRd46wpCjJFGuunuGGfxqveo6zCCR1fw8MczQv";
    private static final String DEFAULT_JWT_TOKEN_AUDIENCE = "this.service.com";
    private static final String DEFAULT_JWT_TOKEN_ISSUER = "this.service.com";
    private static final String DEFAULT_JWT_TOKEN_LIFETIME_IN_SECONDS = "300";

    private final Map<String, String> values = new HashMap<>();

    /**
     * Creates a new EnvironmentValues object pre-set with default values.
     */
    public EnvironmentValues() {
        values.put(RABBITMQ_USER, System.getenv(RABBITMQ_USER) == null
                ? DEFAULT_RABBITMQ_USER : System.getenv(RABBITMQ_USER));

        values.put(RABBITMQ_PASS, System.getenv(RABBITMQ_PASS) == null
                ? DEFAULT_RABBITMQ_PASS : System.getenv(RABBITMQ_PASS));

        values.put(RABBITMQ_HOST, System.getenv(RABBITMQ_HOST) == null
                ? DEFAULT_RABBITMQ_HOST : System.getenv(RABBITMQ_HOST));

        values.put(RABBITMQ_PORT, System.getenv(RABBITMQ_PORT) == null
                ? DEFAULT_RABBITMQ_PORT : System.getenv(RABBITMQ_PORT));

        values.put(WEBAPP_PORT, System.getenv(WEBAPP_PORT) == null
                ? DEFAULT_WEBAPP_PORT : System.getenv(WEBAPP_PORT));

        values.put(MONGO_HOST, System.getenv(MONGO_HOST) == null
                ? DEFAULT_MONGO_HOST : System.getenv(MONGO_HOST));

        values.put(MONGO_PORT, System.getenv(MONGO_PORT) == null
                ? DEFAULT_MONGO_PORT : System.getenv(MONGO_PORT));

        values.put(ADMIN_PASS, System.getenv(ADMIN_PASS) == null
                ? DEFAULT_ADMIN_PASS : System.getenv(ADMIN_PASS));

        values.put(JWT_TOKEN_SECRET, System.getenv(JWT_TOKEN_SECRET) == null
                ? DEFAULT_JWT_TOKEN_SECRET : System.getenv(JWT_TOKEN_SECRET));

        values.put(JWT_TOKEN_AUDIENCE, System.getenv(JWT_TOKEN_AUDIENCE) == null
                ? DEFAULT_JWT_TOKEN_AUDIENCE : System.getenv(JWT_TOKEN_AUDIENCE));

        values.put(JWT_TOKEN_ISSUER, System.getenv(JWT_TOKEN_ISSUER) == null
                ? DEFAULT_JWT_TOKEN_ISSUER : System.getenv(JWT_TOKEN_ISSUER));

        values.put(JWT_TOKEN_LIFETIME_IN_SECONDS, System.getenv(JWT_TOKEN_LIFETIME_IN_SECONDS) == null
                ? DEFAULT_JWT_TOKEN_LIFETIME_IN_SECONDS : System.getenv(JWT_TOKEN_LIFETIME_IN_SECONDS));
    }

    /**
     * Returns the value of an environment variable.
     *
     * @param key - Environment variable name
     * @return value - Environment variable value in string format
     */
    public String getStringValue(final String key) {
        return this.values.get(key);
    }

    /**
     * Returns the value of an environment variable as an Integer.
     *
     * @param key - Environment variable name
     * @return value - Environment variable value in Integer format
     */
    public Integer getIntValue(final String key) {
        try {
            return Integer.parseInt(this.getStringValue(key));
        } catch (Exception e) {
            return null; // EnvironmentVariable was not set
        }
    }

    /**
     * Returns the value of an environment variable as a Long.
     *
     * @param key - Environment variable name
     * @return value - Environment variable value in Long format
     */
    public Long getLongValue(final String key) {
        try {
            return Long.parseLong(this.getStringValue(key));
        } catch (Exception e) {
            return null; // EnvironmentVariable was not set
        }
    }
}
