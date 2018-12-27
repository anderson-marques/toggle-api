package me.marques.anderson.security;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserCredentialsAuthenticator implements Authenticator {

    private static final String TAG = UserCredentialsAuthenticator.class.getName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private final AuthProvider authProvider;

    public UserCredentialsAuthenticator(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public void authenticate(final JsonObject credentials, Handler<AsyncResult<User>> asyncResultHandler) {
        this.authProvider.authenticate(credentials, authenticationResult -> {
            if (authenticationResult.succeeded()) {
                User authenticatedUser = authenticationResult.result();
                asyncResultHandler.handle(Future.succeededFuture(authenticatedUser));
            } else {
                LOGGER.log(Level.WARNING, "Failure to authenticate the user using its credentials",
                        authenticationResult.cause());
                asyncResultHandler.handle(Future.failedFuture(authenticationResult.cause()));
            }
        });
    }
}
