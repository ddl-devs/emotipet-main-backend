package br.com.ifrn.ddldevs.pets_backend.dto.keycloak;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotNull @Schema(description = "Nome de usuário", example = "user123") String username,
        @NotNull @Schema(description = "Senha", example = "coxinha123") String password) {
}
