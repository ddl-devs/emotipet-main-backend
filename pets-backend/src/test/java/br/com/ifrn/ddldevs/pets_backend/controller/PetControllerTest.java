package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.service.PetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PetControllerTest {

    private MockMvc mockMvc;

    private String accessToken;

    private ObjectMapper objectMapper;

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();

        this.mockMvc = MockMvcBuilders.standaloneSetup(petController, authController).build();

        LoginRequestDTO loginRequest = new LoginRequestDTO("clientId", "user", "password",
            "password");

        when(keycloakServiceImpl.generateToken(any(LoginRequestDTO.class)))
            .thenReturn("mocked-jwt-token");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        this.accessToken = result.getResponse().getContentAsString();
        System.out.println(this.accessToken);
    }

    @Test
    @DisplayName("Deve listar pets com sucesso")
    void shouldListPetsSuccessfully() throws Exception {
        mockMvc.perform(get("/pets/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar um pet com sucesso")
    void shouldCreatePetSuccessfully() throws Exception {
        mockMvc.perform(post("/pets/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Buddy")
                .param("species", "DOG")
                .param("gender", "MALE"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar um pet com sucesso")
    void shouldGetPetSuccessfully() throws Exception {
        Long petId = 1L;
        mockMvc.perform(get("/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve atualizar um pet com sucesso")
    void shouldUpdatePetSuccessfully() throws Exception {
        Long petId = 1L;
        mockMvc.perform(put("/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated Buddy"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar um pet com sucesso")
    void shouldDeletePetSuccessfully() throws Exception {
        Long petId = 1L;
        mockMvc.perform(delete("/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}
