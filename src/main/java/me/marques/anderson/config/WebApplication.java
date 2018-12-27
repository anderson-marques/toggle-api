package me.marques.anderson.config;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import me.marques.anderson.api.ToggleResource;
import me.marques.anderson.api.ToggleValuesResource;
import me.marques.anderson.api.TogglesResource;
import me.marques.anderson.api.TokenResource;
import me.marques.anderson.security.Authorizer;

import java.util.logging.Logger;

public class WebApplication {

    private static final String TAG = WebApplication.class.getName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private static final String ADMIN_ROLE = "admin";
    private static final String PONG = "pong";

    private HttpServer server;
    private Router router;

    private Vertx vertx;
    private TogglesResource togglesResource;
    private ToggleResource toggleResource;
    private ToggleValuesResource toggleValuesResource;
    private TokenResource tokenResource;
    private FailureHandler failureHandler;
    private Authorizer authorizer;
    private RoutesConfig routesConfig;

    /**
     * WebApplication default constructor.
     *
     * @param builder - Builder
     */
    private WebApplication(Builder builder) {
        LOGGER.info("Initializing Web Application...");
        this.vertx = builder.vertx;
        this.toggleResource = builder.toggleResource;
        this.togglesResource = builder.togglesResource;
        this.toggleValuesResource = builder.toggleValuesResource;
        this.tokenResource = builder.tokenResource;
        this.failureHandler = builder.failureHandler;
        this.authorizer = builder.authorizer;
        this.routesConfig = builder.routesConfig;

        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);

        Handler<RoutingContext> defaultFailureHandler = failureHandler.handleFailures();

        router.route()
                .handler(BodyHandler.create())
                .consumes("application/json")
                .produces("application/json");

        router.post(routesConfig.getOauthTokenPath())
                .handler(tokenResource.issueTokenHandler())
                .failureHandler(defaultFailureHandler);

        router.route(routesConfig.getTogglesPath()).handler(authorizer.enforceAuthenticated());
        router.route(routesConfig.getTogglePath()).handler(authorizer.enforceAuthenticated());

        router.post(routesConfig.getTogglesPath())
                .handler(authorizer.enforceRoles(ADMIN_ROLE))
                .handler(togglesResource.createToggleHandler())
                .failureHandler(defaultFailureHandler);

        router.get(routesConfig.getTogglesPath())
                .handler(authorizer.enforceRoles(ADMIN_ROLE))
                .handler(togglesResource.listTogglesHandler())
                .failureHandler(defaultFailureHandler);

        router.get(routesConfig.getTogglePath())
                .handler(authorizer.enforceRoles(ADMIN_ROLE))
                .handler(toggleResource.findToggleByNameHandler())
                .failureHandler(defaultFailureHandler);

        router.put(routesConfig.getTogglePath())
                .handler(authorizer.enforceRoles(ADMIN_ROLE))
                .handler(toggleResource.updateToggleHandler())
                .failureHandler(defaultFailureHandler);

        router.get(routesConfig.getTogglesValuesPath())
                .handler(toggleValuesResource.listTogglesValuesHandler())
                .failureHandler(defaultFailureHandler);

        // Health check endpoint
        router.get(routesConfig.getPingPath()).handler(res -> res.response().end(PONG));

        server.requestHandler(router);
    }

    private WebApplication() {
        // Omits the defaults constructor
    }

    /**
     * Starts the WebApplication.
     *
     * @param resultHandler - startResultHandler
     * @param port - listener port
     */
    public void start(int port, Handler<AsyncResult<Void>> resultHandler) {
        this.server.requestHandler(this.router);

        this.server.listen(port, result -> {
            if (result.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Vertx vertx;
        private TogglesResource togglesResource;
        private ToggleResource toggleResource;
        private ToggleValuesResource toggleValuesResource;
        private TokenResource tokenResource;
        private FailureHandler failureHandler;
        private Authorizer authorizer;
        private RoutesConfig routesConfig;

        private Builder() {
        }

        public void setVertx(Vertx vertx) {
            this.vertx = vertx;
        }

        public void setTogglesResource(TogglesResource togglesResource) {
            this.togglesResource = togglesResource;
        }

        public void setToggleResource(ToggleResource toggleResource) {
            this.toggleResource = toggleResource;
        }

        public void setToggleValuesResource(ToggleValuesResource toggleValuesResource) {
            this.toggleValuesResource = toggleValuesResource;
        }

        public void setTokenResource(TokenResource tokenResource) {
            this.tokenResource = tokenResource;
        }

        public void setFailureHandler(FailureHandler failureHandler) {
            this.failureHandler = failureHandler;
        }

        public void setAuthorizer(Authorizer authorizer) {
            this.authorizer = authorizer;
        }

        public void setRoutesConfig(RoutesConfig routesConfig) {
            this.routesConfig = routesConfig;
        }

        public WebApplication build() {
            return new WebApplication(this);
        }
    }
}
