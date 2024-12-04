package br.com.ifrn.ddldevs.pets_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class KeycloakException extends RuntimeException {
    public KeycloakException(String message) {
        super(message);
    }
}
