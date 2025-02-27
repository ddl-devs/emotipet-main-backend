package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.security.AuthUserDetails;
import br.com.ifrn.ddldevs.pets_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/users")
@Tag(name = "Users", description = "API for Users management")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "List users")
    @GetMapping("/")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @Operation(summary = "Get logged user data")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getKeycloakId()));
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create new user")
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @ModelAttribute UserRequestDTO body) {
        return ResponseEntity.ok(userService.createUser(body));
    }

    @Operation(summary = "Update a user")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<UserResponseDTO> updateUser(
        @PathVariable Long id,
        @ModelAttribute UserUpdateRequestDTO body,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.updateUser(id, body, userDetails.getKeycloakId()));
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        userService.deleteUser(id, userDetails.getKeycloakId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "All pets of a logged user")
    @GetMapping("/my-pets")
    public ResponseEntity<List<PetResponseDTO>> getPetsOfCurrentUser(
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getPetsOfCurrentUser(userDetails.getKeycloakId()));
    }

    @Operation(summary = "Get all pets of a user")
    @GetMapping("/pets")
    public ResponseEntity<List<PetResponseDTO>> getPets(
        @AuthenticationPrincipal AuthUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getPets(userDetails.getKeycloakId()));
    }
}
