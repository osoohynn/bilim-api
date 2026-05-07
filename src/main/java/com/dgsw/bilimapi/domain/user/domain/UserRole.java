package com.dgsw.bilimapi.domain.user.domain;

public enum UserRole {
    USER,
    ADMIN;

    public String getKey() {
        return "ROLE_" + this.name();
    }
}
