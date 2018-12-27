package me.marques.anderson.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Toggle {

    @JsonProperty
    private String name;
    @JsonProperty(required = true)
    private Boolean defaultValue;

    private Toggle() {
    }

    public Toggle(String name, Boolean defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @JsonProperty
    private final List<String> audience = new ArrayList<>();
    @JsonProperty
    private final List<String> blacklist = new ArrayList<>();

    @JsonProperty()
    private final List<ToggleUser> overriddenValues = new ArrayList<>();

    public static Toggle createFromJson(JsonObject jsonObject) {
        try {
            return new ObjectMapper().readValue(jsonObject.toString(), Toggle.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAudience() {
        return audience;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public boolean isRestricted() {
        return !canBeUsedByAllServices();
    }

    public boolean canBeUsedByAllServices() {
        return audience.isEmpty() && blacklist.isEmpty();
    }

    public void addAudience(Service service) {
        this.audience.add(service.getIdentifier());
    }

    public boolean canBeUsedBy(String serviceIdentifier) {
        if (canBeUsedByAllServices()) {
            return true;
        } else if (blacklist.contains(serviceIdentifier)) {
            return false;
        } else  {
            return (audience.isEmpty() || audience.contains(serviceIdentifier));
        }
    }

    public boolean cannotBeUsedBy(String serviceIdentifier) {
        return !canBeUsedBy(serviceIdentifier);
    }

    public void denyUseTo(Service service) {
        this.blacklist.add(service.getIdentifier());
    }

    public Boolean getUserValue(String serviceIdentifier, int serviceVersion) {
        for (ToggleUser overriddenValue : this.overriddenValues) {
            if (new ToggleUser(serviceIdentifier, serviceVersion).equals(overriddenValue)) {
                return overriddenValue.getOverriddenValue();
            }
        }
        return defaultValue;
    }

    public void setUserValue(ToggleUser service) {
        if (service.getOverriddenValue() != null) {
            this.overriddenValues.add(service);
        }
    }

    public JsonObject toJson() {
        try {
            return new JsonObject(new ObjectMapper().writeValueAsString(this));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public String toJsonString() {
        return this.toJson().toString();
    }
}
