package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.security.AuthUserDetails;
import br.com.ifrn.ddldevs.pets_backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@Tag(name = "Recommendations", description = "API for Pet Recommendations management")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/")
    @Operation(summary = "Create new recommendation")
    public ResponseEntity<RecommendationResponseDTO> createRecommendation(
        @Valid @RequestBody RecommendationRequestDTO recommendationRequestDTO,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                recommendationService.createRecommendation(
                    recommendationRequestDTO,
                    userDetails.getKeycloakId()
                )
            );
    }

    @GetMapping("/")
    @Operation(summary = "List recommendations")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public List<RecommendationResponseDTO> listRecommendations() {
        return recommendationService.listRecommendations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recommendation by id")
    public RecommendationResponseDTO getRecommendation(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return recommendationService.getRecommendation(id, userDetails.getKeycloakId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recommendation by id")
    public ResponseEntity<Void> deleteRecommendation(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        recommendationService.deleteRecommendation(id, userDetails.getKeycloakId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("pet/{id}")
    @Operation(summary = "Get recommendations by pet id")
    public ResponseEntity<Page<RecommendationResponseDTO>> getRecommendationsByPetId(
                @PathVariable Long id,
                @AuthenticationPrincipal AuthUserDetails userDetails,
                Pageable pageable,
                @RequestParam(required = false) String category,
                @RequestParam(required = false)LocalDate startDate,
                @RequestParam(required = false)LocalDate endDate
            ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            recommendationService.getAllByPetId(id, userDetails.getKeycloakId(), pageable, category, startDate, endDate)
        );
    }
}