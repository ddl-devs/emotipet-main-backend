package br.com.ifrn.ddldevs.pets_backend.keycloak;

import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LogoutRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.RefreshTokenRequestDTO;

public interface KeycloakService {

    String generateToken(LoginRequestDTO dto);

    String refreshToken(RefreshTokenRequestDTO dto);

    String logout(LogoutRequestDTO dto);

    KcUserResponseDTO createUser(UserRequestDTO dto);

    KcUserResponseDTO updateUser(String keycloakId, UserUpdateRequestDTO dto);

    void deleteUser(String keycloakId);
}
