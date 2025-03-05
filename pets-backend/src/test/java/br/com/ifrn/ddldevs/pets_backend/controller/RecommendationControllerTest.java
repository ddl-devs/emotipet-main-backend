package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.service.RecommendationService;
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
public class RecommendationControllerTest {

    private MockMvc mockMvc;
    private String accessToken;
    private ObjectMapper objectMapper;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(recommendationController, authController)
            .build();

        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "admin", "pets-backend",
            "password");

        when(keycloakServiceImpl.generateToken(any(LoginRequestDTO.class)))
            .thenReturn("mocked-jwt-token");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        this.accessToken = result.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Deve listar recomendações com sucesso")
    void shouldListRecommendationsSuccessfully() throws Exception {
        mockMvc.perform(get("/recommendations/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar uma recomendação com sucesso")
    void shouldCreateRecommendationSuccessfully() throws Exception {
        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(1L,
            RecommendationCategories.HEALTH);

        mockMvc.perform(post("/recommendations/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar uma recomendação com sucesso")
    void shouldGetRecommendationSuccessfully() throws Exception {
        Long recommendationId = 1L;
        mockMvc.perform(get("/recommendations/" + recommendationId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar uma recomendação com sucesso")
    void shouldDeleteRecommendationSuccessfully() throws Exception {
        Long recommendationId = 1L;
        mockMvc.perform(delete("/recommendations/" + recommendationId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}
