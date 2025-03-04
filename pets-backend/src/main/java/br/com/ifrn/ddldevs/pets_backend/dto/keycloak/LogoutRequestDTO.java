package br.com.ifrn.ddldevs.pets_backend.dto.keycloak;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LogoutRequestDTO(
        @NotNull @Schema(description = "refresh_token") String refresh_token
) {
}
