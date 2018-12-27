package me.marques.anderson.services;

import io.vertx.core.Future;
import me.marques.anderson.domain.Toggle;
import me.marques.anderson.repository.ToggleRepository;

import java.util.HashMap;
import java.util.Map;

public class ToggleValuesServiceV1 implements ToggleValuesService {

    private final ToggleRepository toggleRepository;

    public ToggleValuesServiceV1(ToggleRepository toggleRepository) {
        this.toggleRepository = toggleRepository;
    }

    @Override
    public Future<Map<String, Boolean>> listValues(String identifier, int version) {
        Future<Map<String, Boolean>> future = Future.future();

        this.toggleRepository.listAll().setHandler(ar -> {
            if (ar.succeeded()) {
                Map<String, Boolean> toggleValues = new HashMap<>();
                for (Toggle toggle : ar.result()) {
                    if (toggle.canBeUsedBy(identifier)) {
                        toggleValues.put(
                                toggle.getName(),
                                toggle.getUserValue(identifier, version)
                        );
                    }
                }

                future.complete(toggleValues);
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }
}
