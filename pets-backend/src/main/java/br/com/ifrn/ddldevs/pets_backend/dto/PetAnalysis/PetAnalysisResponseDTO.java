package br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PetAnalysisResponseDTO(
        @Schema(description = "Id of the Analysis", example = "1") Long id,
        @Schema(description = "Date and time of Analysis creation", example = "2024-12-05T14:30:00Z") LocalDateTime createdAt,
        @Schema(description = "Date and time the Analysis was last updated", example = "2024-12-05T14:30:00Z") LocalDateTime updatedAt,
        @Schema(description = "Picture URL", example = "http://example.com/pet-analysis/picture.jpg") String picture,
        @Schema(description = "Result of the Analysis", example = "Healthy") String result,
        @Schema(description = "Result of the Analysis", example = "93.212831") Double accuracy,
        @Schema(description = "Type of the Analysis", example = "EMOTIONAL") AnalysisType analysisType,
        @Schema(description = "Status of the Analysis", example = "IN_ANA") AnalysisStatus analysisStatus
) {}