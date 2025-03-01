package br.com.ifrn.ddldevs.pets_backend.keycloak;

import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LogoutRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.KeycloakException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.realm}")
    String realmName;

    @Value("${keycloak.server-url}")
    String serverUrl;

    private final Keycloak keycloak;

    public KeycloakServiceImpl(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Override
    public String generateToken(LoginRequestDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate rt = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", dto.clientId());
        formData.add("username", dto.username());
        formData.add("password", dto.password());
        formData.add("grant_type", dto.grantType());

        HttpEntity<MultiValueMap<String, String>> entity =
            new HttpEntity<MultiValueMap<String, String>>(formData, headers);

        try {
            var response = rt.exchange(
                serverUrl + "/protocol/openid-connect/token",
                HttpMethod.POST,
                entity,
                String.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new KeycloakException("Dados não encontrados no Keycloak - " + e.getStatusCode());
        }
    }

    @Override
    public String logout(LogoutRequestDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate rt = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", dto.client_id());
        formData.add("refresh_token", dto.refresh_token());

        HttpEntity<MultiValueMap<String, String>> entity =
            new HttpEntity<MultiValueMap<String, String>>(formData, headers);

        try {
            var response = rt.postForEntity(
                serverUrl + "/protocol/openid-connect/logout",
                entity,
                String.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new KeycloakException("Erro ao fazer logout no Keycloak" + e.getStatusCode());
        }
    }

    public UsersResource getUsersResource() {
        RealmResource realm = keycloak.realm(realmName);
        return realm.users();
    }

    @Override
    public KcUserResponseDTO createUser(UserRequestDTO dto) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.password());
        credential.setTemporary(false);

        user.setCredentials(Collections.singletonList(credential));

        try {
            user.setGroups(Collections.singletonList("client"));
        } catch (KeycloakException e) {
            throw new KeycloakException("Grupo client não existente!");
        }

        UsersResource usersResource = getUsersResource();

        try {
            Response response = usersResource.create(user);
            System.out.println("Response Status: " + response.getStatus());
            System.out.println("Response Body: " + response.readEntity(String.class));

            URI location = response.getLocation();
            String userId = location.getPath().replaceAll(".*/([^/]+)$", "$1");

            UserRepresentation createdUser = keycloak.realm(realmName)
                .users()
                .get(userId)
                .toRepresentation();

            return new KcUserResponseDTO(
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getEmail(),
                createdUser.getFirstName(),
                createdUser.getLastName()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public KcUserResponseDTO updateUser(String keycloakId, UserUpdateRequestDTO dto) {
        UserResource userResource = getUsersResource().get(keycloakId);

        UserRepresentation userRepresentation = userResource.toRepresentation();

        userRepresentation.setEmail(dto.email());
        userRepresentation.setFirstName(dto.firstName());
        userRepresentation.setLastName(dto.lastName());

        try {
            userResource.update(userRepresentation);

            UserRepresentation updatedUser = userResource.toRepresentation();

            return new KcUserResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName()
            );
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while updating user: " + e.getMessage(),
                e);
        }
    }

    @Override
    public void deleteUser(String keycloakId) {
        try {
            UserResource userResource = getUsersResource().get(keycloakId);
            userResource.remove();
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }
}
