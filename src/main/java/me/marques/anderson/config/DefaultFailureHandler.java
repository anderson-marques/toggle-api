package me.marques.anderson.config;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import me.marques.anderson.repository.DocumentConflictException;
import me.marques.anderson.repository.DocumentNotFoundException;

public class DefaultFailureHandler implements FailureHandler {

    /**
     * Defines the default failure handler to the routes.
     *
     * @return failureHandler
     */
    @Override
    public Handler<RoutingContext> handleFailures() {
        return failureRoutingContext -> {
            HttpServerResponse response = failureRoutingContext.response();
            Throwable cause = failureRoutingContext.failure();

            if (cause instanceof IllegalArgumentException) {
                response.setStatusCode(400).end();
            } else if (cause instanceof IllegalAccessException) {
                response.setStatusCode(403).end();
            } else if (cause instanceof DocumentNotFoundException) {
                response.setStatusCode(404).end();
            } else if (cause instanceof DocumentConflictException) {
                response.setStatusCode(409).end();
            } else {
                response.setStatusCode(500).end();
            }
        };
    }
}
