package me.marques.anderson.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToggleUser implements Service {

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty
    private Integer version;
    @JsonProperty
    private Boolean overriddenValue;

    private ToggleUser() {}

    public ToggleUser(String identifier, Integer version) {
        this.identifier = identifier;
        this.version = version;
    }

    public ToggleUser(String identifier, Integer version, boolean overriddenValue) {
        this.identifier = identifier;
        this.version = version;
        this.overriddenValue = overriddenValue;
    }

    public Boolean getOverriddenValue() {
        return overriddenValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    private Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ToggleUser)) {
            return false;
        }

        if (this.identifier.equals(((ToggleUser) other).getIdentifier()) &&
                this.version.equals(((ToggleUser) other).getVersion())) {
            return true;
        }

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, version);
    }
}
