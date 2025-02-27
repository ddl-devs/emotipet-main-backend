package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.security.AuthUserDetails;
import br.com.ifrn.ddldevs.pets_backend.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pets")
@Tag(name = "Pets", description = "API for Pets management")
public class PetController {

    @Autowired
    private PetService petService;

    @Operation(summary = "Create new pet for the current user")
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ResponseEntity<PetResponseDTO> createPet(
        @Valid @ModelAttribute PetRequestDTO petRequestDTO,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(petService.createPet(petRequestDTO, userDetails.getKeycloakId()));
    }

    @Operation(summary = "List pets")
    @GetMapping("/")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public ResponseEntity<List<PetResponseDTO>> listPets() {
        return ResponseEntity.ok(petService.listPets());
    }

    @Operation(summary = "Update a pet")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PetResponseDTO> updatePet(
        @PathVariable Long id,
        @Valid @ModelAttribute PetUpdateRequestDTO petRequestDTO,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(petService.updatePet(id, petRequestDTO, userDetails.getKeycloakId()));
    }

    @Operation(summary = "Get pet by id")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPet(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.ok(petService.getPet(id, userDetails.getKeycloakId()));
    }

    @Operation(summary = "Delete a pet")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        petService.deletePet(id, userDetails.getKeycloakId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
