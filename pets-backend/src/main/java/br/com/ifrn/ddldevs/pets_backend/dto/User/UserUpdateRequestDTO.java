package br.com.ifrn.ddldevs.pets_backend.dto.User;


import br.com.ifrn.ddldevs.pets_backend.validator.MinAge;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UserUpdateRequestDTO(
    @Size(min=1) @Email @Schema(description = "User's email", example = "user" +
        "@gmail" +
        ".com") String email,
    @Size(min=1) @Schema(description = "User's firstname", example = "user") String firstName,
    @Size(min=1) @Schema(description = "User's lastname", example = "silva") String lastName,
    @MinAge @Schema(description = "User's " +
        "birthdate", example = "2024-12-05T14:30:00Z") LocalDate dateOfBirth,
    @Schema(description = "User's profile photo url", example = "aws.12bs.bucket.com") MultipartFile photoUrl
) {

}
