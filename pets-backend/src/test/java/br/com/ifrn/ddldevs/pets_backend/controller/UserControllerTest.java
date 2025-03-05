package br.com.ifrn.ddldevs.pets_backend.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.ifrn.ddldevs.pets_backend.SecurityTestConfig;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import br.com.ifrn.ddldevs.pets_backend.service.UploadImageService;
import br.com.ifrn.ddldevs.pets_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "AWS_BUCKET_NAME=emotipet-bucket"
})
@ActiveProfiles("test")
@Import({SecurityTestConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeycloakServiceImpl keycloakServiceImpl;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private UploadImageService uploadImageService;

    @MockitoBean
    MockMultipartFile mockImage = new MockMultipartFile(
        "file",
        "user-picture.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        UserRequestDTO userRequest = new UserRequestDTO(
            "1abc23",
            "john@#@email.com",
            "John",
            "Doe",
            LocalDate.of(1990, 1, 15),
            mockImage,
            "user!123"
        );

        user = userMapper.toEntity(userRequest);
        user.setKeycloakId(UUID.randomUUID().toString());
        user = userRepository.save(user);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("Deve listar usuários com sucesso")
    void shouldListUsersSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve retornar Forbidden ao listar usuários com token de client")
    void shouldListUsersNotSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @DisplayName("Deve buscar usuário logado com sucesso")
    void shouldGetCurrentUserSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        String expectedResponse = objectMapper.writeValueAsString(
            userMapper.toResponseDTO(user)
        );

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/me")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(expectedResponse));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso quando o usuário é admin")
    void shouldGetUserByIdSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + 1L)
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void shouldCreateUserSuccessfully() throws Exception {
        userRepository.deleteAll();

        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        MockMultipartFile mockImage = new MockMultipartFile(
            "file",
            "user-picture.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );

        KcUserResponseDTO kcUserResponseDTO = new KcUserResponseDTO(
            user.getKeycloakId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName()
        );

        when(keycloakServiceImpl.createUser(any())).thenReturn(kcUserResponseDTO);

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/users/")
                    .file(mockImage) // Enviar o arquivo de imagem
                    .header("Authorization", "Bearer " + tokenString)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .param("username", "john")
                    .param("email", "john@example.com")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("dateOfBirth", "2000-10-07")
                    .param("password", "user!123")
            )
            .andExpect(MockMvcResultMatchers.status()
                .isOk());
    }


    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void shouldUpdateUserSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/{id}", user.getId())
                    .header("Authorization", "Bearer " + tokenString)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .param("name", "John Doe")
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso")
    void shouldDeleteUserSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        Mockito.doNothing().when(keycloakServiceImpl).deleteUser(user.getKeycloakId());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users/{id}", user.getId())
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar pets do usuário logado com sucesso")
    void shouldGetCurrentUserPetsSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/my-pets")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve buscar todos os pets de um usuário com sucesso")
    void shouldGetUserPetsSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/pets")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

