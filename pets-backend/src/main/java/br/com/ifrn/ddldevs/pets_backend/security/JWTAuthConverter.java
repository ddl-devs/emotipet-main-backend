package br.com.ifrn.ddldevs.pets_backend.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

// transforms the JWT token received from Keycloak into a CustomAuthToken instance
// see Jwt token roles in https://jwt.io/

@Component
public class JWTAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${keycloak.client-id}")
    String keycloakClientId;

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()
            ),
            extractRealmRoles(jwt).stream()
        ).collect(Collectors.toSet());

        var authUserDetails = new AuthUserDetails(
            jwt.getClaimAsString("sub"),
            jwt.getClaimAsString("preferred_username"),
            authorities
        );

        return new CustomAuthToken(
            authUserDetails,
            null,
            authorities
        );
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (jwt.getClaim("resource_access") == null) {
            return Set.of();
        }

        resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess.get(keycloakClientId) == null) {
            return Set.of();
        }
        resource = (Map<String, Object>) resourceAccess.get(keycloakClientId);

        resourceRoles = (Collection<String>) resource.get("roles");
        return resourceRoles
            .stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        if (jwt.getClaim("realm_access") == null) {
            return Set.of();
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");

        return realmRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
    }
}
