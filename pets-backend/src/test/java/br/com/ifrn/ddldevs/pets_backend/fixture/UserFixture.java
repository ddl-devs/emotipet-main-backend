package br.com.ifrn.ddldevs.pets_backend.fixture;

import br.com.ifrn.ddldevs.pets_backend.domain.User;
import java.time.LocalDate;
import java.util.List;

public class UserFixture {

    public static User createValid(Long id) {
        return User.builder()
            .id(id)
            .keycloakId("test-keycloak-id")
            .username("test-username")
            .firstName("Test")
            .lastName("User")
            .email("testuser@example.com")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .photoUrl("http://example.com/photo.jpg")
            .pets(List.of())
            .build();
    }

    public static List<User> createListValid() {
        return List.of(createValid(1L), createValid(2L));
    }
}
