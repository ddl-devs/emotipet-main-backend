package br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Represents a request for Pet Analysis")
public class PetAnalysisRequestDTO {
    @Schema(description = "Id of the Pet", example = "1")
    @NotNull
    @Valid
    private Long petId;

    @NotNull
    @Schema(description = "Picture URL", example = "http://example.com/pet-analysis/picture.jpg")
    private String picture;

    @NotNull
    @Schema(description = "Type of the Analysis", example = "Blood Test")
    private AnalysisType analysisType;

}