package br.com.ifrn.ddldevs.pets_backend.dto.Pet;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para atualização de Pets")
public class PetUpdateRequestDTO {

    @Schema(description = "Pet's name", example = "Apolo")
    @Valid
    @Size(min = 1, message = "Pet's name can't be empty")
    private String name;

    @Schema(description = "Pet's sex", example = "MALE")
    @Valid
    private Gender gender;

    @Schema(description = "Pet's birthdate", example = "2")
    @Valid
    @PastOrPresent
    private LocalDate birthdate;

    @Schema(description = "Pet's weight (kg)", example = "2.5")
    @Valid
    @Positive(message = "Peso (kg) deve ser maior que zero")
    private BigDecimal weight;

    @Schema(description = "Pet's species")
    private Species species;

    @Schema(description = "Pet's breed", example = "Yorkshire")
    @Valid
    @Size(min = 1, message = "Pet's breed can't be empty")
    private String breed;

    @Schema(description = "Pet's height (cm)", example = "30")
    @Valid
    @Positive(message = "Altura (cm) deve ser maior que zero")
    private Integer height;

    @Schema(description = "Pet's photo", example = "www.foto.com")
    @Valid
    private MultipartFile photoUrl;

}
