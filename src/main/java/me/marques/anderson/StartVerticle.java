package me.marques.anderson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.mongo.HashAlgorithm;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import me.marques.anderson.api.ToggleResource;
import me.marques.anderson.api.ToggleValuesResource;
import me.marques.anderson.api.TogglesResource;
import me.marques.anderson.api.TokenResource;
import me.marques.anderson.config.*;
import me.marques.anderson.events.EventsGateway;
import me.marques.anderson.repository.ToggleRepository;
import me.marques.anderson.repository.ToggleRepositoryV1;
import me.marques.anderson.security.*;
import me.marques.anderson.services.ToggleValuesService;
import me.marques.anderson.services.ToggleValuesServiceV1;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.marques.anderson.config.EnvironmentValues.*;

class StartVerticle extends AbstractVerticle {

    private static final String ADMIN = "admin";
    private final EnvironmentValues environmentValues;
    private MongoAuth mongoAuthProvider;
    private MongoClient mongoClient;
    private EventsGateway eventsGateway;

    StartVerticle(final EnvironmentValues environmentValues) {
        this.environmentValues = environmentValues;
    }

    private static final Logger LOGGER = Logger.getLogger(StartVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) {
        Future<Void> initializationSteps =
                startMessageBroker().compose(ar1 -> startMongo().compose(ar2 -> startWebApplication()));
        initializationSteps.setHandler(startFuture);
    }

    private Future<Void> startMongo() {
        Future<Void> future = Future.future();
        LOGGER.info("Initializing MongoDB...");
        try {
            JsonObject dbConfig = new JsonObject();
            dbConfig.put("host", this.environmentValues.getStringValue(MONGO_HOST));
            dbConfig.put("port", this.environmentValues.getIntValue(MONGO_PORT));

            this.mongoClient = MongoClient.createNonShared(vertx, dbConfig);

            JsonObject authProperties = new JsonObject();
            this.mongoAuthProvider = MongoAuth.create(mongoClient, authProperties);
            mongoAuthProvider.setHashAlgorithm(HashAlgorithm.PBKDF2);

            createUsernameIndexAndAdminUser();

            LOGGER.info("MongoClient initialized");
            future.complete();
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, "Failed to create MongoClient", t);
            future.fail(t);
        }
        return future;
    }

    private void createUsernameIndexAndAdminUser() {
        mongoClient.createIndex("user", new JsonObject()
                        .put("username", 1)
                        .put("unique", true)
                , createAdminUser());
    }

    private Handler<AsyncResult<Void>> createAdminUser() {
        return res -> {
            if (res.succeeded()) {
                LOGGER.info("username index created in user collection!");

                // Try to find the admin user. If it does not exists, create it.
                mongoClient.findOne("user", new JsonObject().put("username", ADMIN), null, findAdminRes -> {
                    if (findAdminRes.succeeded() && findAdminRes.result() == null) {
                        List<String> roles = new ArrayList<>();
                        roles.add(ADMIN);
                        List<String> permissions = new ArrayList<>();
                        String password = this.environmentValues.getStringValue(EnvironmentValues.ADMIN_PASS);
                        mongoAuthProvider.insertUser(ADMIN, password, roles, permissions, saveRes -> {
                            if (saveRes.succeeded()) {
                                LOGGER.info("admin user created in user collection!");
                            } else {
                                LOGGER.log(Level.WARNING, "Failed to create admin user!", saveRes.cause());
                            }
                        });
                    }
                });
            } else {
                LOGGER.log(Level.WARNING, "Failed to create username index in user collection!", res.cause());
            }
        };
    }

    private Future<Void> startWebApplication() {
        Future<Void> future = Future.future();

        JWTAuth jwtAuth = createJwtAuth();

        ToggleRepository toggleRepository = new ToggleRepositoryV1(mongoClient);
        TogglesResource togglesResource = new TogglesResource(eventsGateway, toggleRepository);
        ToggleResource toggleResource = new ToggleResource(eventsGateway, toggleRepository);
        ToggleValuesService toggleValuesService = new ToggleValuesServiceV1(toggleRepository);
        ToggleValuesResource toggleValuesResource = new ToggleValuesResource(toggleValuesService);
        SecurityTokenGenerator securityTokenGenerator = new JwtTokenGenerator(environmentValues, jwtAuth);
        Authenticator authenticator = new UserCredentialsAuthenticator(this.mongoAuthProvider);
        TokenResource tokenResource = new TokenResource(authenticator, environmentValues, securityTokenGenerator);
        FailureHandler failureHandler = new DefaultFailureHandler();

        Authorizer authorizer = new JwtAuthorizer(jwtAuth);

        Integer port = this.environmentValues.getIntValue(EnvironmentValues.WEBAPP_PORT);

        RoutesConfig routesConfig = new RoutesConfig();

        WebApplication.Builder builder = WebApplication.createBuilder();

        builder.setVertx(vertx);
        builder.setAuthorizer(authorizer);
        builder.setFailureHandler(failureHandler);
        builder.setRoutesConfig(routesConfig);
        builder.setToggleResource(toggleResource);
        builder.setTogglesResource(togglesResource);
        builder.setToggleValuesResource(toggleValuesResource);
        builder.setTokenResource(tokenResource);

        builder.build().start(port, ar -> {
            if (ar.succeeded()) {
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }

    private JWTAuth createJwtAuth() {
        return JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey(this.environmentValues.getStringValue(JWT_TOKEN_SECRET))
                        .setSymmetric(true)));
    }

    private Future<Void> startMessageBroker() {
        Future<Void> future = Future.future();
        LOGGER.info("Initializing RabbitMQ...");
        try {
            RabbitMQOptions options = new RabbitMqConfig(this.environmentValues).getOptions();
            RabbitMQClient client = RabbitMQClient.create(vertx, options);
            this.eventsGateway = new EventsGateway(client);
            this.eventsGateway.start();
            future.complete();
        } catch (Exception e) {
            LOGGER.info("RabbitMQ failed to start");
            future.fail(e);
        }

        return future;
    }
}
