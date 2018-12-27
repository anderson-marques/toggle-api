package me.marques.anderson.api;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import me.marques.anderson.config.EnvironmentValues;
import me.marques.anderson.security.Authenticator;
import me.marques.anderson.security.InvalidRequestException;
import me.marques.anderson.security.SecurityTokenGenerator;
import me.marques.anderson.security.UnsupportedGrantTypeException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenResource {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String GRANT_TYPE = "grant_type";
    private static final String PASS = "password";
    private static final String USERNAME = "username";

    private static final String TAG = TokenResource.class.getName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String ERROR = "error";
    private static final String APPLICATION_JSON = "application/json";

    private final Authenticator authenticator;
    private final EnvironmentValues environmentValues;
    private final SecurityTokenGenerator securityTokenGenerator;

    /**
     * TokenResource is the endpoint responsible for issue access tokens used by this application.
     *
     * @param authenticator          - Authenticator responsible to ensure that the credentials are correct.
     * @param environmentValues      - EnvironmentValues that holds app configuration values.
     * @param securityTokenGenerator - Generates the access token.
     */
    public TokenResource(final Authenticator authenticator, final EnvironmentValues environmentValues,
                         final SecurityTokenGenerator securityTokenGenerator) {
        this.authenticator = authenticator;
        this.environmentValues = environmentValues;
        this.securityTokenGenerator = securityTokenGenerator;
    }

    /**
     * Issue access tokens according to RFC 6749, OAuth 2.0 Resource Owner Password Credentials.
     *
     * <p>The authorization server is also the resource server. The scope and client authentication
     * are dismissed.
     *
     * @return tokenResponse - token response in JsonObject format
     */
    public Handler<RoutingContext> issueTokenHandler() {
        return routingContext -> {
            HttpServerRequest request = routingContext.request();
            HttpServerResponse response = routingContext.response();
            request.setExpectMultipart(true);
            request.endHandler(res -> {

                String grantType = request.getFormAttribute(GRANT_TYPE);
                String username = request.getFormAttribute(USERNAME);
                String password = request.getFormAttribute(PASS);

                try {
                    validateGrantType(grantType);
                    validateCredentials(username, password);

                    JsonObject credentials = new JsonObject();
                    credentials.put(USERNAME, username);
                    credentials.put(PASS, password);

                    this.authenticator.authenticate(credentials, authenticationResult -> {
                        if (authenticationResult.succeeded()) {
                            User user = authenticationResult.result();
                            long lifetimeInSeconds =
                                    environmentValues.getLongValue(EnvironmentValues.JWT_TOKEN_LIFETIME_IN_SECONDS);
                            String accessToken = this.securityTokenGenerator.generate(user, lifetimeInSeconds);

                            JsonObject accessTokenResponse = new JsonObject();
                            accessTokenResponse.put(ACCESS_TOKEN, accessToken);
                            accessTokenResponse.put(EXPIRES_IN, lifetimeInSeconds);

                            response.setStatusCode(200).putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                    .end(accessTokenResponse.toString());
                        } else {
                            final String errorDescription = "Invalid username or password.";
                            LOGGER.log(Level.WARNING, errorDescription, authenticationResult.cause());
                            JsonObject errorResponse = new JsonObject();
                            errorResponse.put(ERROR, "invalid_grant");
                            errorResponse.put(ERROR_DESCRIPTION, errorDescription);
                            response.setStatusCode(400)
                                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                    .end(errorResponse.toString());
                        }
                    });
                } catch (InvalidRequestException e) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.put(ERROR, "invalid_request");
                    errorResponse.put(ERROR_DESCRIPTION, e.getMessage());
                    response.setStatusCode(400)
                            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(errorResponse.toString());
                } catch (UnsupportedGrantTypeException e) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.put(ERROR, "unsupported_grant_type");
                    errorResponse.put(ERROR_DESCRIPTION, e.getMessage());
                    response.setStatusCode(400)
                            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(errorResponse.toString());
                }
            });
        };
    }

    private void validateGrantType(
            final String grantType) throws InvalidRequestException, UnsupportedGrantTypeException {
        if (grantType == null) {
            throw new InvalidRequestException("Parameter grant_type is required.");
        } else if (!grantType.equals(PASS)) {
            throw new UnsupportedGrantTypeException("Grant type not supported");
        }
    }

    private void validateCredentials(
            final String username,
            final String password) throws InvalidRequestException {
        if (username == null || password == null) {
            throw new InvalidRequestException("Invalid credentials");
        }
    }
}
