package br.com.ifrn.ddldevs.pets_backend.dto.Pet;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PetResponseDTO(
        @Schema(description = "Pet's ID", example = "1") Long id,
        @Schema(description = "Date and time of Pet creation", example = "2024-12-05T14:30:00Z") LocalDateTime createdAt,
        @Schema(description = "Date and time the Pet was last updated", example = "2024-12-05T14:30:00Z") LocalDateTime updatedAt,
        @Schema(description = "Pet's name", example = "Apolo")String name,
        @Schema(description = "Pet's sex", example = "MALE") Gender gender,
        @Schema(description = "Pet's birthdate", example = "2024-12-05") LocalDate birthdate,
        @Schema(description = "Pet's weight (kg)", example = "2.5") BigDecimal weight,
        @Schema(description = "Pet's breed", example = "Yorkshire")String breed,
        @Schema(description = "Pet's species", example = "Dog") Species species,
        @Schema(description = "Pet's height (cm)", example = "30")Integer height,
        @Schema(description = "Pet's photo", example = "www.foto.com") String photoUrl
){}
