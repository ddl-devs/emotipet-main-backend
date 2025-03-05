package br.com.ifrn.ddldevs.pets_backend.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserControllerTest {

    private MockMvc mockMvc;
    private String accessToken;
    private ObjectMapper objectMapper;

    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AuthController authController;

    @MockitoBean
    MockMultipartFile mockImage = new MockMultipartFile(
        "picture",
        "pet-analysis-picture.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController, authController).build();

        LoginRequestDTO loginRequest = new LoginRequestDTO("luke", "user!123", "pets-backend",
            "password");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        this.accessToken = result.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Deve listar usuários com sucesso")
    void shouldListUsersSuccessfully() throws Exception {
        mockMvc.perform(get("/users/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar usuário logado com sucesso")
    void shouldGetCurrentUserSuccessfully() throws Exception {
        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso quando o usuário é admin")
    void shouldGetUserByIdSuccessfully() throws Exception {
        Long userId = 1L;
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void shouldCreateUserSuccessfully() throws Exception {
        mockMvc.perform(multipart("/users/")
                .file(mockImage)
                .header("Authorization", "Bearer " + accessToken)
                .param("username", "John Doe")
                .param("email", "john@example.com")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("dateOfBirth", "2000-10-07")
                .param("password", "user!123"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void shouldUpdateUserSuccessfully() throws Exception {
        Long userId = 1L;
        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated John"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso")
    void shouldDeleteUserSuccessfully() throws Exception {
        Long userId = 1L;
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar pets do usuário logado com sucesso")
    void shouldGetCurrentUserPetsSuccessfully() throws Exception {
        mockMvc.perform(get("/users/my-pets")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar todos os pets de um usuário com sucesso")
    void shouldGetUserPetsSuccessfully() throws Exception {
        mockMvc.perform(get("/users/pets")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }
}

