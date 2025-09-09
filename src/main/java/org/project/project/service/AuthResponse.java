package org.project.project.service;

import lombok.Getter;
import org.project.project.model.entity.Usuario;

/**
 * DTO para respuestas de autenticación
 */
@Getter
public class AuthResponse {
    // Getters
    private final boolean success;
    private final String message;
    private final String error;
    private final String redirectUrl;
    private final Usuario user;

    private AuthResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.error = builder.error;
        this.redirectUrl = builder.redirectUrl;
        this.user = builder.user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private String error;
        private String redirectUrl;
        private Usuario user;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder redirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public Builder user(Usuario user) {
            this.user = user;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(this);
        }
    }
}
