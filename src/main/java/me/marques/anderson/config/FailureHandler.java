package me.marques.anderson.config;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface FailureHandler {

    /**
     * Defines the default failure handler to the routes.
     *
     * @return failureHandler
     */
    Handler<RoutingContext> handleFailures();

}
