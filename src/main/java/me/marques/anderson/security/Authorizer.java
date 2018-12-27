package me.marques.anderson.security;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface Authorizer {

    Handler<RoutingContext> enforceAuthenticated();

    Handler<RoutingContext> enforceRoles(String... allowedRoles);

}