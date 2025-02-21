package br.com.ifrn.ddldevs.pets_backend.dto.User;

import br.com.ifrn.ddldevs.pets_backend.validator.MinAge;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserRequestDTO(
    @NotNull @Schema(description = "User's name",
        example = "user123") String username,
    @NotNull @Email @Schema(description = "User's email", example = "user" +
        "@gmail" +
        ".com") String email,
    @NotNull @Schema(description = "User's firstname", example = "user") String firstName,
    @NotNull @Schema(description = "User's lastname", example = "silva") String lastName,
    @MinAge @Schema(description = "User's " +
        "birthdate", example = "2024-12-05") LocalDate dateOfBirth,
    @Schema(description = "User's profile photo url", example = "aws.12bs.bucket.com") String photoUrl,
    @NotNull @Schema(description = "User's password",
        example = "test123") String password
) {

}
