package me.marques.anderson.api;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import me.marques.anderson.domain.Toggle;
import me.marques.anderson.events.EventsGateway;
import me.marques.anderson.repository.ToggleRepository;

public class ToggleResource {

    private final ToggleRepository toggleRepository;
    private final EventsGateway eventsGateway;

    public ToggleResource(
            EventsGateway eventsGateway,
            ToggleRepository toggleRepository) {
        this.eventsGateway = eventsGateway;
        this.toggleRepository = toggleRepository;
    }

    public Handler<RoutingContext> updateToggleHandler() {
        return routingContext -> {
            try {
                final String name = routingContext.pathParam("name");

                Toggle toggle = Toggle.createFromJson(routingContext.getBodyAsJson());

                if (toggle.getName() != null && !name.equals(toggle.getName())) {
                    routingContext.fail(new IllegalAccessException("Toggle name does not match the object."));
                }
                toggle.setName(name);
                toggleRepository.update(toggle).setHandler(ar -> {
                    if (ar.succeeded()) {
                        eventsGateway.publishEvent("domain", "toggle_changed", toggle.toJsonString());
                        routingContext.response()
                                .putHeader("Content-type", "application/json")
                                .end(toggle.toJsonString());
                    } else {
                        routingContext.fail(ar.cause());
                    }
                });
            } catch (Exception e) {
                routingContext.fail(e);
            }
        };
    }

    public Handler<RoutingContext> findToggleByNameHandler() {
        return routingContext -> {
            final String name = routingContext.pathParam("name");

            this.toggleRepository.findByName(name).setHandler(ar -> {
                if (ar.succeeded()) {
                    routingContext.response()
                            .putHeader("Content-type", "application/json")
                            .end(ar.result().toJsonString());
                } else {
                    routingContext.fail(ar.cause());
                }
            });
        };
    }
}
