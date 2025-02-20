package br.com.ifrn.ddldevs.pets_backend.security;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class CustomAuthToken extends AbstractAuthenticationToken {

    private final AuthUserDetails principal;
    private Object credentials;

    public CustomAuthToken(
        AuthUserDetails principal,
        Object credentials,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }
}