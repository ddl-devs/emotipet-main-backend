package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.SecurityTestConfig;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.RecommendationMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.microservice.RecommendationRequestsService;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.RecommendationRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
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
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TokenUtils tokenUtils;

    @MockitoBean
    private KeycloakServiceImpl keycloakServiceImpl;

    @MockitoBean
    private RecommendationRequestsService recommendationRequestsService;

    @MockitoBean
    MockMultipartFile mockImage = new MockMultipartFile(
        "file",
        "user-picture.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    private User user;

    private Pet pet;

    private Recommendation recommendation;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        UserRequestDTO userRequest = new UserRequestDTO(
            "alex",
            "alex@email.com",
            "Alex",
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
        pet = petRepository.saveAndFlush(pet);

        Pet savedPet = petRepository.findById(pet.getId())
            .orElseThrow(() -> new RuntimeException("Pet not found"));

        RecommendationRequestDTO recommendationRequest = new RecommendationRequestDTO(
            savedPet.getId(),
            RecommendationCategories.HEALTH
        );

        recommendation = recommendationMapper.toEntity(recommendationRequest);
        recommendation.setPet(savedPet);
        recommendation = recommendationRepository.save(recommendation);
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        recommendationRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve listar recomendações com sucesso")
    void shouldListRecommendationsSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/recommendations/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve retornar Forbidden ao listar recomendações com token de client")
    void shouldListRecommendationsNotSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/recommendations/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @DisplayName("Deve criar uma recomendação com sucesso")
    void shouldCreateRecommendationSuccessfully() throws Exception {
        recommendationRepository.deleteAll();

        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        String requestBody = String.format("""
            {
                "petId": %d,
                "categoryRecommendation": "HEALTH"
            }
            """, pet.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/recommendations/")
                    .header("Authorization", "Bearer " + tokenString)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar uma recomendação com sucesso")
    void shouldGetRecommendationSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(get("/recommendations/{id}", recommendation.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar uma recomendação com sucesso")
    void shouldDeleteRecommendationSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(delete("/recommendations/{id}", recommendation.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isNoContent());
    }
}
