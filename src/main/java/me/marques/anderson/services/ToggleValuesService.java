package me.marques.anderson.services;

import io.vertx.core.Future;

import java.util.Map;

public interface ToggleValuesService {

    /**
     * List available toggles and its values for a specific user.
     *
     * @param identifier - Toggle user service identifier
     * @param version    - Toggle user service version
     * @return togglesValues - Map with toggle name and the toggle value for the service
     */
    Future<Map<String, Boolean>> listValues(String identifier, int version);

}