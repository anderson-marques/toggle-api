package me.marques.anderson.config;

import io.vertx.rabbitmq.RabbitMQOptions;

import static me.marques.anderson.config.EnvironmentValues.RABBITMQ_HOST;
import static me.marques.anderson.config.EnvironmentValues.RABBITMQ_PASS;
import static me.marques.anderson.config.EnvironmentValues.RABBITMQ_USER;
import static me.marques.anderson.config.EnvironmentValues.RABBITMQ_PORT;


public class RabbitMqConfig {

    private final RabbitMQOptions options;
    private final EnvironmentValues environmentValues;

    public RabbitMqConfig(final EnvironmentValues environmentValues) {
        this.environmentValues = environmentValues;
        this.options = defineOptions();

    }

    public RabbitMQOptions getOptions() {
        return this.options;
    }

    private RabbitMQOptions defineOptions() {
        return new RabbitMQOptions().setUri(getConnectionUri());
    }

    private String getConnectionUri() {
        return "amqp://" + environmentValues.getStringValue(RABBITMQ_USER) + ":" +
                environmentValues.getStringValue(RABBITMQ_PASS) + "@" +
                environmentValues.getStringValue(RABBITMQ_HOST) + ":" +
                environmentValues.getStringValue(RABBITMQ_PORT);
    }
}
