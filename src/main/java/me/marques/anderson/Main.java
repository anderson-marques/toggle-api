package me.marques.anderson;

import io.vertx.core.Vertx;
import me.marques.anderson.config.EnvironmentValues;

class Main {

    public static void main(String[] args) {
        new Main().start();
    }

    /**
     * Starts the application deploying the StartVerticle.
     */
    private void start() {
        // Create the Verticle
        Vertx vertx = Vertx.vertx();

        // Obtain the EnvironmentValues
        EnvironmentValues environmentValues = new EnvironmentValues();

        StartVerticle startVerticle = new StartVerticle(environmentValues);
        // Deploy PingService
        vertx.deployVerticle(startVerticle);
    }
}