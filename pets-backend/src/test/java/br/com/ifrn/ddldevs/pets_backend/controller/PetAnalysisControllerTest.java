package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.LoginRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.service.PetAnalysisService;
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
public class PetAnalysisControllerTest {

    private MockMvc mockMvc;
    private String accessToken;
    private ObjectMapper objectMapper;

    @Mock
    private PetAnalysisService petAnalysisService;

    @InjectMocks
    private PetAnalysisController petAnalysisController;

    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;

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
        this.mockMvc = MockMvcBuilders.standaloneSetup(petAnalysisController, authController)
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
    @DisplayName("Deve listar análises com sucesso quando o usuário é admin")
    void shouldListAnalysisSuccessfully() throws Exception {
        mockMvc.perform(get("/pet-analysis/")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

//    @Test
//    @DisplayName("Deve retornar Forbidden quando o usuário não é admin")
//    void shouldReturnForbiddenWhenUserNotAdmin() throws Exception {
//        mockMvc.perform(get("/pet-analysis/")
//                .header("Authorization",
//                    "Bearer " + "UMTuYS2ef4BEQdT9T91PRCCPGVsjLxHB3Hcp3jdmayZIfKm0PaZEaWgrovA9LKaQ")
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isForbidden());
//    }

    @Test
    @DisplayName("Deve criar uma análise com sucesso")
    void shouldCreateAnalysisSuccessfully() throws Exception {
        mockMvc.perform(multipart("/pet-analysis/")
                .file(mockImage)
                .header("Authorization", "Bearer " + accessToken)
                .param("analysisType", AnalysisType.BREED.toString())
                .param("petId", "1")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar uma análise com sucesso")
    void shouldGetAnalysisSuccessfully() throws Exception {
        Long analysisId = 1L;
        mockMvc.perform(get("/pet-analysis/" + analysisId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar uma análise com sucesso")
    void shouldDeleteAnalysisSuccessfully() throws Exception {
        Long analysisId = 1L;
        mockMvc.perform(delete("/pet-analysis/" + analysisId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}