package br.com.ifrn.ddldevs.pets_backend.dto.Recommendation;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents a request for a recommendation")
public class RecommendationRequestDTO {

    @Schema(description = "Id of the pet", example = "1")
    @NotNull(message = "Pet's id is mandatory")
    @Valid
    private Long petId;

    @Schema(description = "Category of the recommendation", example = "Nutrition")
    @NotNull
    private RecommendationCategories categoryRecommendation;
}