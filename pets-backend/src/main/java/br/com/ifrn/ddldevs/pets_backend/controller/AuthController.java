package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LogoutRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.RefreshTokenRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.KeycloakException;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${keycloak.realm}")
    String realmName;

    @Autowired
    private KeycloakServiceImpl keycloakServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO body) {
        return ResponseEntity.ok(keycloakServiceImpl.generateToken(body));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequestDTO body) {
        return ResponseEntity.ok(keycloakServiceImpl.logout(body));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequestDTO dto) {
        try {
            String newToken = keycloakServiceImpl.refreshToken(dto);
            return ResponseEntity.ok(newToken);
        } catch (KeycloakException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
