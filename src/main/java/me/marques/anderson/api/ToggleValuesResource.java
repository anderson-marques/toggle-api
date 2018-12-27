package me.marques.anderson.api;

import io.vertx.core.Handler;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import me.marques.anderson.services.ToggleValuesService;

import java.util.Map;

public class ToggleValuesResource {

    private static final int OK_STATUS_CODE = 200;
    private final ToggleValuesService listToggleValuesService;

    public ToggleValuesResource(final ToggleValuesService listToggleValuesService) {
        this.listToggleValuesService = listToggleValuesService;
    }

    public Handler<RoutingContext> listTogglesValuesHandler() {
        return routingContext -> {
            try {
                String identifier = routingContext.pathParam("identifier");
                int version = Integer.parseInt(routingContext.pathParam("version"));
                this.listToggleValuesService.listValues(identifier, version).setHandler(ar -> {
                    if (ar.succeeded()) {
                        routingContext.response().setStatusCode(OK_STATUS_CODE);
                        JsonObject values = new JsonObject();
                        for (Map.Entry<String, Boolean> valueEntry : ar.result().entrySet()) {
                            values.put(valueEntry.getKey(), valueEntry.getValue());
                        }
                        routingContext.response()
                                .putHeader("Content-Type", "application/json")
                                .end(values.toString());
                    } else {
                        routingContext.fail(ar.cause());
                    }
                });
            } catch (IllegalArgumentException | EncodeException e) {
                routingContext.fail(e);
            }
        };
    }
}
