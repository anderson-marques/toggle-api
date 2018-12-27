package me.marques.anderson.security;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

public interface Authenticator {

    void authenticate(JsonObject credentials, Handler<AsyncResult<User>> asyncResultHandler);

}