package br.com.ifrn.ddldevs.pets_backend.dto.Recommendation;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RecommendationResponseDTO(
    @Schema(description = "Recommendation's ID", example = "1") Long id,
    @Schema(description = "Date and time of the Recommendation creation", example = "2024-12-05T14:30:00Z") LocalDateTime createdAt,
    @Schema(description = "Date and time the Recommendation was last updated", example = "2024-12-05T14:30:00Z") LocalDateTime updatedAt,
    @Schema(description = "Details of the recommendation", example = "Feed your pet twice daily") String recommendation,
    @Schema(description = "Category of the recommendation", example = "Health") RecommendationCategories categoryRecommendation
) {

}