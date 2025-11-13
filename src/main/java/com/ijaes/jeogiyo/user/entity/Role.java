package com.ijaes.jeogiyo.user.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER"),
    OWNER("ROLE_OWNER"),
    MANAGER("ROLE_MANAGER"),
    BLOCK("ROLE_BLOCK");

    private final String authority;

    public String getAuthority() {
        return authority;
    }
}
