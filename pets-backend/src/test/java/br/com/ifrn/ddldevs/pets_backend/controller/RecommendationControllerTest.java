package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.mapper.RecommendationMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.RecommendationRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private PetRepository petRepository;

    private Pet pet;

    private User user;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    @Transactional
    public void setUp() throws Exception {
        recommendationRepository.deleteAll();
        petRepository.deleteAll();

        User new_user = new User();
        new_user.setUsername("jhon");
        new_user.setFirstName("Jhon");
        new_user.setLastName("Doe");
        new_user.setEmail("jhon@gmail.com");
        new_user.setKeycloakId("345");
        new_user.setPets(new ArrayList<>());

        Pet pet = new Pet();
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));

        new_user.getPets().add(pet);
        pet.setUser(new_user);

        this.user = userRepository.save(new_user);
        this.pet = petRepository.save(pet);
    }

    @Test
    @DisplayName("Should create an recommendation successfully")
    @Transactional
    public void createRecommendationSuccessfully() throws Exception {
        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(this.pet.getId(),
            "Feed your pet twice daily", "Nutrition");

        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(
            post("/recommendations/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated());

        Optional<Recommendation> savedRecommendation = recommendationRepository.findAll().stream()
            .findFirst();
        assertTrue(savedRecommendation.isPresent());
        assertEquals("Feed your pet twice daily", savedRecommendation.get().getRecommendation());
    }

    @Test
    @DisplayName("Should delete an recommendation successfully")
    @Transactional
    public void deleteRecommendationSuccessfully() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setPet(this.pet);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation("Nutrition");
        recommendation.setCreatedAt(LocalDateTime.now());

        recommendation = recommendationRepository.save(recommendation);

        mockMvc.perform(
            delete("/recommendations/" + recommendation.getId())
        ).andExpect(MockMvcResultMatchers.status().isNoContent());

        Optional<Recommendation> savedRecommendation = recommendationRepository.findById(
            recommendation.getId());
        assertFalse(savedRecommendation.isPresent());
    }

    @Test
    @DisplayName("Should get an recommendation successfully")
    @Transactional
    public void getRecommendationSuccessfully() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setPet(this.pet);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation("Nutrition");
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation = recommendationRepository.save(recommendation);

        String expectedResponse = objectMapper.writeValueAsString(
            recommendationMapper.toRecommendationResponseDTO(recommendation));

        mockMvc.perform(
                get("/recommendations/" + recommendation.getId())
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    @DisplayName("Should get recommendations by pet id")
    @Transactional
    public void getRecommendationsByPetIdSuccessfully() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setPet(this.pet);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation("Nutrition");
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation = recommendationRepository.save(recommendation);

        Recommendation recommendation2 = new Recommendation();
        recommendation2.setPet(this.pet);
        recommendation2.setRecommendation("Feed your pet once daily");
        recommendation2.setCategoryRecommendation("Nutrition");
        recommendation2.setCreatedAt(LocalDateTime.now());
        recommendation2 = recommendationRepository.save(recommendation2);

        List<Recommendation> recommendationList = new ArrayList<>();
        recommendationList.add(recommendation);
        recommendationList.add(recommendation2);

        String expectedResponse = objectMapper.writeValueAsString(
            recommendationMapper.toDTOList(recommendationList));

        mockMvc.perform(
                get("/recommendations/pet/" + this.pet.getId())
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    @DisplayName("Should list pets sucessfully")
    @Transactional
    public void listPetsSuccessfully() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setPet(this.pet);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation("Nutrition");
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation = recommendationRepository.save(recommendation);

        Recommendation recommendation2 = new Recommendation();
        recommendation2.setPet(this.pet);
        recommendation2.setRecommendation("Feed your pet once daily");
        recommendation2.setCategoryRecommendation("Nutrition");
        recommendation2.setCreatedAt(LocalDateTime.now());
        recommendation2 = recommendationRepository.save(recommendation2);

        List<Recommendation> recommendationList = new ArrayList<>();
        recommendationList.add(recommendation);
        recommendationList.add(recommendation2);

        String expectedResponse = objectMapper.writeValueAsString(
            recommendationMapper.toDTOList(recommendationList));

        mockMvc.perform(
                get("/recommendations/")
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

    }
}