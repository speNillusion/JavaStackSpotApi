// No arquivo EnsureDto.java
package org.stackspotapi.dto;

import java.time.Instant;

public class EnsureDto {
    private String jwt;
    private Instant tokenExpiry;
    // Você pode adicionar outros campos como executionId se precisar deles no mesmo objeto
    // private String executionId;

    // Construtores, getters e setters

    public EnsureDto() {
    }

    public EnsureDto(String jwt, Instant tokenExpiry) {
        this.jwt = jwt;
        this.tokenExpiry = tokenExpiry;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Instant tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
}
