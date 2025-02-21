package br.com.ifrn.ddldevs.pets_backend.dto.Pet;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para requisições de Pets")
public class PetRequestDTO {

    @Schema(description = "Pet's name", example = "Apolo")
    @Valid
    @NotNull
    @NotBlank
    private String name;

    @Schema(description = "Pet's sex", example = "MALE")
    @Valid
    private Gender gender;

    @Schema(description = "Pet's age", example = "2")
    @Min(0)
    private Integer age;

    @Schema(description = "Pet's weight (kg)", example = "2.5")
    @Valid
    @Positive(message = "Peso (kg) deve ser maior que zero")
    private BigDecimal weight;

    @Schema(description = "Pet's species")
    @Valid
    @NotNull(message = "Pet's species is required!")
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
    @Size(min = 1, message = "Photo url can't be empty")
    @Valid
    private String photoUrl;
}
