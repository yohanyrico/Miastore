package com.tiendaropa.model;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public String getName() {
        return this.name();
    }
}