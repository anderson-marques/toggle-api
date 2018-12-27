package me.marques.anderson.security;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JwtAuthorizer implements Authorizer {

    private static final String NOT_AUTHORIZED_MESSAGE = "Not authorized";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TAG = JwtAuthorizer.class.getName();
    private static final Logger LOGGER = Logger.getLogger(TAG);
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_ACCESS_TOKEN = "Bearer [accessToken]";

    private final JWTAuth jwtProvider;

    public JwtAuthorizer(JWTAuth authProvider) {
        this.jwtProvider = authProvider;
    }

    @Override
    public Handler<RoutingContext> enforceAuthenticated() {
        return routingContext -> {
            HttpServerRequest request = routingContext.request();
            HttpServerResponse response = routingContext.response();

            try {
                String authorizationHeader = request.getHeader(AUTHORIZATION);

                if (authorizationHeader == null || authorizationHeader.split(" ").length != 2
                        || !authorizationHeader.split(" ")[0].equals("Bearer")) {
                    throw new AuthenticationFailedException(NOT_AUTHORIZED_MESSAGE);
                }

                final String[] authorizationParts = authorizationHeader.split(" ");

                final String accessToken = authorizationParts[1];
                JsonObject credentials = new JsonObject().put("jwt", accessToken);

                this.jwtProvider.authenticate(credentials, authenticationResult -> {
                    if (authenticationResult.succeeded() && authenticationResult.result() != null) {
                        User authenticatedUser = authenticationResult.result();
                        LOGGER.info("User authenticated: " + authenticatedUser.principal().toString());
                        routingContext.setUser(authenticatedUser);
                        routingContext.next();
                    } else {
                        LOGGER.log(Level.WARNING, NOT_AUTHORIZED_MESSAGE, authenticationResult.cause());
                        response.setStatusCode(401).putHeader(WWW_AUTHENTICATE, BEARER_ACCESS_TOKEN)
                                .end(NOT_AUTHORIZED_MESSAGE);
                    }
                });

            } catch (AuthenticationFailedException e) {
                LOGGER.log(Level.WARNING, NOT_AUTHORIZED_MESSAGE, e);
                response.setStatusCode(401).putHeader(WWW_AUTHENTICATE, BEARER_ACCESS_TOKEN).end(NOT_AUTHORIZED_MESSAGE);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, NOT_AUTHORIZED_MESSAGE, e);
                routingContext.fail(e);
            }
        };
    }

    @Override
    public Handler<RoutingContext> enforceRoles(String... allowedRoles) {
        return routingContext -> {
            HttpServerResponse response = routingContext.response();
            User user = routingContext.user();
            boolean userHasRole = false;

            if (allowedRoles == null || allowedRoles.length == 0) {
                routingContext.next();
            } else if (user == null) {
                response.setStatusCode(401).putHeader(WWW_AUTHENTICATE, BEARER_ACCESS_TOKEN).end(NOT_AUTHORIZED_MESSAGE);
            } else {
                for (String allowedRole : allowedRoles) {
                    if (user.principal().getJsonArray("roles").contains(allowedRole)) {
                        userHasRole = true;
                        break;
                    }
                }
            }

            if (userHasRole) {
                routingContext.next();
            } else {
                LOGGER.log(Level.WARNING, NOT_AUTHORIZED_MESSAGE);
                response.setStatusCode(401).putHeader(WWW_AUTHENTICATE, BEARER_ACCESS_TOKEN).end(NOT_AUTHORIZED_MESSAGE);
            }
        };
    }
}
