package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.dto.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.LogoutRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${keycloak.realm}")
    String realmName;

    @Autowired
    private KeycloakService keycloakService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO body) {
        return ResponseEntity.ok(keycloakService.generateToken(body));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequestDTO body) {
        return ResponseEntity.ok(keycloakService.logout(body));
    }
}
