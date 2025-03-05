package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.SecurityTestConfig;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({SecurityTestConfig.class})
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @MockitoBean
    private KeycloakServiceImpl keycloakServiceImpl;

    @MockitoBean
    MockMultipartFile mockImage = new MockMultipartFile(
        "file",
        "user-picture.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    private User user;

    private Pet pet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        UserRequestDTO userRequest = new UserRequestDTO(
            "maik",
            "maik@email.com",
            "Maik",
            "Doe",
            LocalDate.of(1990, 1, 15),
            mockImage,
            "user!123"
        );

        user = userMapper.toEntity(userRequest);
        user.setKeycloakId(UUID.randomUUID().toString());
        user = userRepository.save(user);

        PetRequestDTO petRequest = new PetRequestDTO(
            "Apolo",
            Gender.MALE,
            LocalDate.of(2020, 1, 15),
            BigDecimal.valueOf(10.0),
            Species.DOG,
            "Labrador",
            30,
            mockImage
        );

        pet = petMapper.toEntity(petRequest);
        pet.setUser(user);
        pet = petRepository.save(pet);
    }

    @AfterEach
    public void tearDown() {
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve listar pets com sucesso")
    void shouldListPetsSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/pets/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve retornar Forbidden ao listar pets com token de client")
    void shouldListPetsNotSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/pets/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Deve criar um pet com sucesso")
    void shouldCreatePetSuccessfully() throws Exception {
        petRepository.deleteAll();

        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(post("/pets/")
                .header("Authorization", "Bearer " + tokenString)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Buddy")
                .param("species", "DOG")
                .param("gender", "MALE"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar um pet com sucesso")
    void shouldGetPetSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(get("/pets/" + pet.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve atualizar um pet com sucesso")
    void shouldUpdatePetSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(put("/pets/{id}", pet.getId())
                .header("Authorization", "Bearer " + tokenString)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated Buddy"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar um pet com sucesso")
    void shouldDeletePetSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(delete("/pets/{id}", pet.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isNoContent());
    }
}
