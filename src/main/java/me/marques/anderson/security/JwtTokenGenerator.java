package me.marques.anderson.security;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import me.marques.anderson.config.EnvironmentValues;

import java.time.Duration;
import java.util.UUID;

import static me.marques.anderson.config.EnvironmentValues.JWT_TOKEN_AUDIENCE;
import static me.marques.anderson.config.EnvironmentValues.JWT_TOKEN_ISSUER;

public class JwtTokenGenerator implements SecurityTokenGenerator {

    private static final String USERNAME = "username";

    private final EnvironmentValues environmentValues;
    private final JWTAuth jwtAuth;

    public JwtTokenGenerator(final EnvironmentValues environmentValues, final JWTAuth jwtAuth) {
        this.environmentValues = environmentValues;
        this.jwtAuth = jwtAuth;
    }

    @Override
    public String generate(final User user, final long secondsToExpire) {
        JsonObject principal = user.principal();
        JsonObject claims = new JsonObject();

        claims.put("jti", UUID.randomUUID().toString());
        claims.put("roles", principal.getJsonArray("roles"));
        return jwtAuth.generateToken(claims,
                new JWTOptions().addAudience(environmentValues.getStringValue(JWT_TOKEN_AUDIENCE))
                        .setExpiresInMinutes((int) Duration.ofSeconds(secondsToExpire).toMinutes())
                        .setIssuer(environmentValues.getStringValue(JWT_TOKEN_ISSUER))
                        .setSubject(principal.getString(USERNAME)));
    }
}
