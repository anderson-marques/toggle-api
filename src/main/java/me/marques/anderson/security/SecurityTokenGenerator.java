package me.marques.anderson.security;

import io.vertx.ext.auth.User;

public interface SecurityTokenGenerator {

    String generate(final User user, final long secondsToExpire);

}
