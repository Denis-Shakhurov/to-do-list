package org.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleUser {
    USER("user"),
    ADMIN("admin");

    private final String value;

    RoleUser(String value) {
        this.value = value;
    }

    @JsonCreator
    public static RoleUser fromString(String value) throws IllegalAccessException {
        for (RoleUser role : RoleUser.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalAccessException("Unknown role: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
