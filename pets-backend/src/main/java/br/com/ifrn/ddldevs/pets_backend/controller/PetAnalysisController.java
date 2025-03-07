package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.security.AuthUserDetails;
import br.com.ifrn.ddldevs.pets_backend.service.PetAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pet-analysis")
@RequiredArgsConstructor
@Tag(name = "Pets Analysis", description = "API for Pets Analysis management")
public class PetAnalysisController {

    @Autowired
    private PetAnalysisService petAnalysisService;

    @PostMapping(value = "/", consumes = "multipart/form-data")
    @Operation(summary = "Create new Pet Analysis")
    public ResponseEntity<PetAnalysisResponseDTO> createPetAnalysis(
        @Valid @ModelAttribute PetAnalysisRequestDTO petAnalysisRequestDTO,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            petAnalysisService.createPetAnalysis(
                petAnalysisRequestDTO, userDetails.getKeycloakId()
            )
        );
    }

    @GetMapping("/")
    @Operation(summary = "List Pet Analyses")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public ResponseEntity<List<PetAnalysisResponseDTO>> listPetAnalyses() {
        return ResponseEntity.status(HttpStatus.OK).body(petAnalysisService.listPetAnalyses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Pet Analysis by id")
    public ResponseEntity<PetAnalysisResponseDTO> getPetAnalysis(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            petAnalysisService.getPetAnalysis(id, userDetails.getKeycloakId())
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Pet Analysis by id")
    public ResponseEntity<Void> deletePetAnalysis(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        petAnalysisService.deletePetAnalysis(id, userDetails.getKeycloakId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/pet/{id}")
    @Operation(summary = "Get Pet Analyses by pet id")
    public ResponseEntity<Page<PetAnalysisResponseDTO>> getPetAnalysisByPetId(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) AnalysisType type,
        @RequestParam(required = false) AnalysisStatus status,
        @RequestParam(required = false) String result,
        Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            petAnalysisService.getAllByPetId(id, userDetails.getKeycloakId(),
                    startDate, endDate, type, status, result, pageable)
        );
    }
}