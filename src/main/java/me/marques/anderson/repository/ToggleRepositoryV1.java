package me.marques.anderson.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import me.marques.anderson.domain.Toggle;

import java.util.ArrayList;
import java.util.List;

public class ToggleRepositoryV1 implements ToggleRepository {

    private static final String COLLECTION = "toggles";

    private final MongoClient mongoClient;

    public ToggleRepositoryV1(final MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public Future<Toggle> insert(final Toggle toggle) {
        Future<Toggle> future = Future.future();

        JsonObject document = toggle.toJson();

        document.put("_id", toggle.getName());

        JsonObject query = new JsonObject();
        query.put("_id", toggle.getName());
        mongoClient.findOne(COLLECTION, query, null, ar -> {
            if (ar.succeeded() && ar.result() != null) {
                DocumentConflictException exception =
                        new DocumentConflictException("Already exists a toggle with the provided name.",
                                ar.cause());
                future.fail(exception);
            } else {
                mongoClient.save(COLLECTION, document, res -> {
                    if (res.succeeded()) {
                        future.complete(Toggle.createFromJson(document));
                    } else {
                        future.fail(res.cause());
                    }
                });
            }
        });


        return future;
    }

    @Override
    public Future<Toggle> update(final Toggle toggle) {
        Future<Toggle> future = Future.future();
        try {
            JsonObject document = toggle.toJson();

            document.put("_id", toggle.getName());

            JsonObject query = new JsonObject();
            query.put("_id", toggle.getName());
            mongoClient.findOne(COLLECTION, query, null, ar -> {
                if (ar.succeeded() && ar.result() == null) {
                    future.fail(new DocumentNotFoundException("Toggle not found"));
                } else {
                    mongoClient.save(COLLECTION, document, res -> {
                        if (res.succeeded()) {
                            future.complete(toggle);
                        } else {
                            future.fail(res.cause());
                        }
                    });
                }
            });
        } catch (Exception e) {
            future.fail(e);
        }

        return future;
    }

    @Override
    public Future<List<Toggle>> listAll() {
        Future<List<Toggle>> future = Future.future();
        JsonObject query = new JsonObject();
        List<Toggle> toggles = new ArrayList<>();
        mongoClient.find(COLLECTION, query, res -> {
            if (res.succeeded()) {
                if (!res.result().isEmpty()) {
                    for (JsonObject json : res.result()) {
                        toggles.add(Toggle.createFromJson(json));
                    }
                }
                future.complete(toggles);
            } else {
                future.fail(res.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Toggle> findByName(String name) {
        Future<Toggle> future = Future.future();
        JsonObject query = new JsonObject();
        query.put("_id", name);
        mongoClient.findOne(COLLECTION, query, null, res -> {
            if (res.succeeded()) {
                if (res.result() == null) {
                    DocumentNotFoundException cause = new DocumentNotFoundException("Toggle not found");
                    future.fail(cause);
                } else {
                    future.complete(Toggle.createFromJson(res.result()));
                }
            } else {
                future.fail(res.cause());
            }
        });
        return future;
    }
}
