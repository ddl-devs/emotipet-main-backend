package br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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

    @Valid
    @NotBlank
    @Schema(description = "Picture URL", example = "http://example.com/pet-analysis/picture.jpg")
    private MultipartFile picture;

    @Valid
    @NotNull
    @Schema(description = "Type of the Analysis", example = "EMOTIONAL")
    private AnalysisType analysisType;

}