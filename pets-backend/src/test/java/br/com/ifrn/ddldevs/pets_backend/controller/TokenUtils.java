package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "tMNQw9Ol1iRBjFXekhihhrVY1woEPO3K".getBytes()
    );

    private String keycloakClientId = "pets-backend";

    public String getToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora de expiração
            .signWith(SECRET_KEY)
            .compact();
    }

    public Jwt getJwt(String token, User user, List<String> roles) {
        if (token == null) {
            token = getToken(user.getEmail());
        }

        if (user.getEmail() == null || user.getKeycloakId() == null) {
            throw new IllegalArgumentException("User email or Keycloak ID is missing");
        }

        Map<String, Object> realmAccess = Map.of(
            "roles", roles
        );

        System.out.println("realmAccess: " + realmAccess);

        return new Jwt(
            token, Instant.now(), Instant.now().plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of(
                "preferred_username", user.getEmail(),
                "email", user.getEmail(),
                "sub", user.getKeycloakId(),
                "realm_access", realmAccess
            )
        );
    }


}
