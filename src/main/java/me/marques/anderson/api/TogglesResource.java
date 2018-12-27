package me.marques.anderson.api;

import io.vertx.core.Handler;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import me.marques.anderson.domain.Toggle;
import me.marques.anderson.events.EventsGateway;
import me.marques.anderson.repository.ToggleRepository;

public class TogglesResource {

    private final ToggleRepository toggleRepository;
    private final EventsGateway eventsGateway;

    public TogglesResource(
            final EventsGateway eventsGateway,
            final ToggleRepository toggleRepository) {
        this.eventsGateway = eventsGateway;
        this.toggleRepository = toggleRepository;
    }

    public Handler<RoutingContext> createToggleHandler() {
        return routingContext -> {
            try {
                Toggle newToggle = Toggle.createFromJson(routingContext.getBodyAsJson());

                this.toggleRepository.insert(newToggle).setHandler(res -> {
                    if (res.succeeded()) {
                        eventsGateway.publishEvent("domain", "toggle_created", res.result().toJsonString());
                        routingContext.response().setStatusCode(201);
                        routingContext.response().end(res.result().toJson().toString());
                    } else {
                        routingContext.fail(res.cause());
                    }
                });
            } catch (IllegalArgumentException | EncodeException e) {
                routingContext.fail(e);
            }
        };
    }

    public Handler<RoutingContext> listTogglesHandler() {
        return routingContext -> {
            try {
                this.toggleRepository.listAll().setHandler(ar -> {
                    if (ar.succeeded()) {
                        routingContext
                                .response()
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonArray(ar.result()).toString());
                    } else {
                        routingContext.fail(ar.cause());
                    }
                });
            } catch (Exception e) {
                routingContext.fail(e);
            }
        };
    }
}
